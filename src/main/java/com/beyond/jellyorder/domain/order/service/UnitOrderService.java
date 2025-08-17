package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.option.dto.MainOptionReqDto;
import com.beyond.jellyorder.domain.option.dto.SubOptionReqDto;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import com.beyond.jellyorder.domain.option.subOption.repository.SubOptionRepository;
import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderMenuReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResult;
import com.beyond.jellyorder.domain.order.entity.*;
import com.beyond.jellyorder.domain.order.repository.OrderMenuRepository;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.order.repository.UnitOrderRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitOrderService {

    private final StoreTableRepository storeTableRepository;
    private final TotalOrderRepository totalOrderRepository;
    private final UnitOrderRepository unitOrderRepository;
    private final MenuRepository menuRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final SubOptionRepository subOptionRepository;

    public UnitOrderResDto createUnit(UnitOrderCreateReqDto dto) {
        // 1. 요청값 검증
        validateRequest(dto);

        // 2. 테이블 조회
        StoreTable storeTable = findStoreTable(dto.getStoreTableId());

        // 3. 전체주문 확보
        TotalOrder totalOrder = getOrCreateTotalOrder(storeTable);

        // 4. 단위주문 생성
        UnitOrder unitOrder = createUnitOrder(totalOrder);

        // 5. 메뉴 처리 (재고 검증 + 주문/옵션 저장 + 합산)
        UnitOrderResult result = processMenus(dto.getMenus(), unitOrder);

        // 6. 집계 반영
        updateAggregates(unitOrder, totalOrder, result);

        // 7. 테이블 상태 갱신
        updateTableStatusIfNeeded(storeTable, totalOrder);

        // 8. 응답 생성
        return buildResponse(totalOrder, unitOrder, result);
    }

    /* --------------------- private methods --------------------- */

    private void validateRequest(UnitOrderCreateReqDto dto) {
        if (dto.getMenus() == null || dto.getMenus().isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }
        dto.getMenus().forEach(menuReqDto -> {
            if (menuReqDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("수량은 1개 이상이어야 합니다. 메뉴ID : " + menuReqDto.getMenuId());
            }
        });
    }

    private StoreTable findStoreTable(UUID storeTableId) {
        return storeTableRepository.findById(storeTableId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장의 테이블이 없습니다."));
    }

    private TotalOrder getOrCreateTotalOrder(StoreTable storeTable) {
        return totalOrderRepository.findTopByStoreTableOrderByOrderedAtDesc(storeTable)
                .filter(order -> order.getEndedAt() == null) // 미종료 주문이면 재사용
                .orElseGet(() -> totalOrderRepository.save(
                        TotalOrder.builder()
                                .storeTable(storeTable)
                                .totalPrice(0)
                                .count(0)
                                .build()
                ));
    }

    private UnitOrder createUnitOrder(TotalOrder totalOrder) {
        UnitOrder unitOrder = UnitOrder.builder()
                .totalOrder(totalOrder)
                .status(OrderStatus.ACCEPT)
                .totalCount(0)
                .build();
        return unitOrderRepository.save(unitOrder);
    }

    private UnitOrderResult processMenus(List<UnitOrderMenuReqDto> menus, UnitOrder unitOrder) {
        int unitPrice = 0;
        int unitCount = 0;

        for (UnitOrderMenuReqDto menuReqDto : menus) {
            Menu menu = validateAndUpdateStock(menuReqDto);

            OrderMenu orderMenu = createOrderMenu(menu, menuReqDto, unitOrder);
            int optionsPrice = processOptions(menuReqDto, menu, orderMenu);

            unitPrice += menu.getPrice() * menuReqDto.getQuantity() + optionsPrice;
            unitCount += menuReqDto.getQuantity();
        }

        return new UnitOrderResult(unitPrice, unitCount);
    }

    private Menu validateAndUpdateStock(UnitOrderMenuReqDto menuReqDto) {
        Menu menu = menuRepository.findById(menuReqDto.getMenuId())
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴가 존재하지 않습니다."));

        int stockQuantity = menu.getSalesLimit() - menu.getSalesToday();

        // 품절 시 주문 불가
        if (menu.getStockStatus() == MenuStatus.OUT_OF_STOCK
                || menu.getStockStatus() == MenuStatus.SOLD_OUT_MANUAL) {
            throw new IllegalArgumentException("품절된 상품입니다.");
        }

        // 한정 판매 수량 검증
        if (menu.getSalesLimit() != -1) {
            if (stockQuantity < menuReqDto.getQuantity()) {
                throw new IllegalArgumentException("현재 남은 수량은 " + stockQuantity + "개 입니다.");
            }
            menu.increaseSalesToday(menuReqDto.getQuantity());
            if (menu.getSalesLimit() == menu.getSalesToday()) {
                menu.changeStockStatus(MenuStatus.OUT_OF_STOCK);
            }
        } else { // 상시판매
            menu.increaseSalesToday(menuReqDto.getQuantity());
        }

        return menu;
    }

    private OrderMenu createOrderMenu(Menu menu, UnitOrderMenuReqDto menuReqDto, UnitOrder unitOrder) {
        OrderMenu orderMenu = OrderMenu.builder()
                .unitOrder(unitOrder)
                .menu(menu)
                .quantity(menuReqDto.getQuantity())
                .build();
        return orderMenuRepository.save(orderMenu);
    }

    private int processOptions(UnitOrderMenuReqDto menuReqDto, Menu menu, OrderMenu orderMenu) {
        int optionsPriceTotal = 0;

        if (menuReqDto.getMainOptions() == null || menuReqDto.getMainOptions().isEmpty()) {
            return optionsPriceTotal;
        }

        Map<UUID, Integer> quantityBySubId = new LinkedHashMap<>();
        for (MainOptionReqDto mainDto : menuReqDto.getMainOptions()) {
            if (mainDto.getSubOptions() == null) continue;
            for (SubOptionReqDto subDto : mainDto.getSubOptions()) {
                UUID subId = UUID.fromString(subDto.getSubOptionId());
                quantityBySubId.merge(subId, subDto.getQuantity(), Integer::sum);
            }
        }

        List<SubOption> subOptions = subOptionRepository.findAllById(quantityBySubId.keySet());
        for (SubOption so : subOptions) {
            if (!so.getMainOption().getMenu().getId().equals(menu.getId())) {
                throw new IllegalArgumentException("선택한 옵션이 해당 메뉴의 옵션이 아닙니다.");
            }
            int quantity = quantityBySubId.getOrDefault(so.getId(), 0);
            optionsPriceTotal += so.getPrice() * quantity;

            if (quantity > 0) {
                OrderMenuOption option = OrderMenuOption.builder()
                        .orderMenu(orderMenu)
                        .subOption(so)
                        .optionName(so.getName())
                        .optionPrice(so.getPrice())
                        .optionQuantity(quantity)
                        .build();
                orderMenu.getOrderMenuOptionList().add(option);
            }
        }

        return optionsPriceTotal;
    }

    private void updateAggregates(UnitOrder unitOrder, TotalOrder totalOrder, UnitOrderResult result) {
        unitOrder.updateUnitCount(result.getUnitCount());
        totalOrder.addUnitTotal(result.getUnitPrice(), result.getUnitCount());
    }

    private void updateTableStatusIfNeeded(StoreTable storeTable, TotalOrder totalOrder) {
        if (storeTable.getStatus() != TableStatus.EATING && totalOrder.getOrderedAt() != null) {
            storeTable.changeStatus(TableStatus.EATING);
        }
    }

    private UnitOrderResDto buildResponse(TotalOrder totalOrder, UnitOrder unitOrder, UnitOrderResult result) {
        return UnitOrderResDto.builder()
                .totalOrderId(totalOrder.getId())
                .unitOrderId(unitOrder.getId())
                .unitPrice(result.getUnitPrice())
                .unitCount(result.getUnitCount())
                .build();
    }
}