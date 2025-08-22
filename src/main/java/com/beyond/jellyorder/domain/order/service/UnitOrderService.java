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
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusMenu;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusMenuOption;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.entity.*;
import com.beyond.jellyorder.domain.order.repository.OrderMenuRepository;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.order.repository.UnitOrderRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    public OrderStatusResDTO createUnit(UnitOrderCreateReqDto dto) {
        // 1. 테이블 조회
        StoreTable storeTable = findStoreTable(dto.getStoreTableId());

        // 2. 전체주문 확보
        TotalOrder totalOrder = getOrCreateTotalOrder(storeTable);

        // 3. 단위주문 생성
        UnitOrder unitOrder = createUnitOrder(totalOrder);

        // 4. 메뉴 처리 (재고 검증 + 주문/옵션 저장 + 합산)
        UnitOrderResult result = processMenus(dto.getMenuList(), unitOrder);

        // 5. 집계 반영
        updateAggregates(unitOrder, totalOrder, result);

        // 6. 테이블 상태 갱신
        updateTableStatusIfNeeded(storeTable, totalOrder);

        // 8. 응답 생성
        return buildResponse(unitOrder, storeTable);
    }

    /* --------------------- private methods --------------------- */


    // 1. 테이블 조회
    private StoreTable findStoreTable(UUID storeTableId) {
        return storeTableRepository.findById(storeTableId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장의 테이블이 없습니다."));
    }

    // 2. 전체주문 확보
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

    // 3. 단위주문 생성
    private UnitOrder createUnitOrder(TotalOrder totalOrder) {
        UnitOrder unitOrder = UnitOrder.builder()
                .totalOrder(totalOrder)
                .status(OrderStatus.ACCEPT)
                .totalCount(0)
                .build();
        return unitOrderRepository.save(unitOrder);
    }

    // 4. 메뉴 처리 (재고 검증 + 주문/옵션 저장 + 합산)
    private UnitOrderResult processMenus(List<UnitOrderMenuReqDto> menus, UnitOrder unitOrder) {
        int unitPrice = 0;
        int unitCount = 0;

        for (UnitOrderMenuReqDto menuReqDto : menus) {
            Menu menu = validateAndUpdateStock(menuReqDto);

            // 저장 지연 : 엔티티만 만들고 아직 save 하지 않음
            OrderMenu orderMenu = createOrderMenu(menu, menuReqDto, unitOrder);
            // 옵션 처리
            int optionsPrice = processOptions(menuReqDto, menu, orderMenu);

            // cascade로 옵션까지 저장
            unitOrder.getOrderMenus().add(orderMenu);
            orderMenuRepository.save(orderMenu);

            unitPrice += menu.getPrice() * menuReqDto.getQuantity() + optionsPrice;
            unitCount += menuReqDto.getQuantity();
        }

        return new UnitOrderResult(unitPrice, unitCount);
    }

    // TODO: 추후 redis를 도입하여 동시성 문제 해결.
    private Menu validateAndUpdateStock(UnitOrderMenuReqDto menuReqDto) {
        Menu menu = menuRepository.findById(menuReqDto.getMenuId())
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴가 존재하지 않습니다."));

        int stockQuantity = menu.getSalesLimit() - menu.getSalesToday();

        // 품절 시 주문 불가
        if (menu.getStockStatus() != MenuStatus.ON_SALE) {
            throw new IllegalArgumentException("품절된 상품입니다.");
        }

        // 한정 판매 수량 검증
        if (menu.getSalesLimit() != -1) {
            if (stockQuantity < menuReqDto.getQuantity()) {
                throw new IllegalArgumentException("현재 남은 수량은 " + stockQuantity + "개 입니다.");
            }
            menu.increaseSalesToday(menuReqDto.getQuantity());
            if (menu.getSalesLimit().equals(menu.getSalesToday())) {
                menu.changeStockStatus(MenuStatus.OUT_OF_STOCK);
            }
        } else { // 상시판매
            menu.increaseSalesToday(menuReqDto.getQuantity());
        }

        return menu;
    }

    private OrderMenu createOrderMenu(Menu menu, UnitOrderMenuReqDto menuReqDto, UnitOrder unitOrder) {
        return OrderMenu.builder()
                .unitOrder(unitOrder)
                .menu(menu)
                .menuName(menu.getName())   // 스냅샷
                .menuPrice(menu.getPrice()) // 스냅샷
                .quantity(menuReqDto.getQuantity())
                .build();
    }

    private int processOptions(UnitOrderMenuReqDto menuReqDto, Menu menu, OrderMenu orderMenu) {

        // 1) 요청에서 sub_option.id만 뽑아 중복 제거
        Set<UUID> requested = Optional.ofNullable(menuReqDto.getOptionList())
                .orElseGet(List::of).stream()
                .map(SubOptionReqDto::getSubOptionId)
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (requested.isEmpty()) return 0;

        // 2) 실제 SubOption 조회 + 누락 ID 검출
        List<SubOption> found = subOptionRepository.findAllById(requested);
        if (found.size() != requested.size()) {
            Set<UUID> missing = new LinkedHashSet<>(requested);
            for (SubOption so : found) missing.remove(so.getId());
            throw new EntityNotFoundException("존재하지 않는 subOptionId: " + missing);
        }

        int perMenuQty = Optional.ofNullable(menuReqDto.getQuantity()).orElse(1); // 옵션은 메뉴 수량만큼 적용
        int optionsTotalPrice = 0;

        for (SubOption so : found) {
            // 3) 해당 메뉴의 옵션인지 검증
            if (!so.getMainOption().getMenu().getId().equals(menu.getId())) {
                throw new IllegalArgumentException("다른 메뉴의 옵션입니다: " + so.getId());
            }

            // 4) 가격 합산(서버 신뢰값 사용)
            optionsTotalPrice += so.getPrice() * perMenuQty;

            // 5) 스냅샷 + 양방향 연결 (FK 세팅은 addOption 안에서)
            orderMenu.addOption(
                    OrderMenuOption.builder()
                            .subOption(so)
                            .optionName(so.getName())
                            .optionPrice(so.getPrice())
                            .build()
            );
        }

        return optionsTotalPrice;
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

    private OrderStatusResDTO buildResponse(UnitOrder unitOrder, StoreTable storeTable) {
        // orderMenu → dto 변환
        List<OrderStatusMenu> orderMenuDtos = unitOrder.getOrderMenus().stream()
                .map(orderMenu -> OrderStatusMenu.builder()
                        .menuName(orderMenu.getMenu().getName())   // 스냅샷
                        .menuQuantity(orderMenu.getQuantity())
                        .optionList(
                                orderMenu.getOrderMenuOptionList().stream()
                                        .map(omo -> OrderStatusMenuOption.builder()
                                                .optionName(omo.getSubOption().getName())
                                                .build()
                                        ).toList()
                        )
                        .build()
                ).toList();

        return OrderStatusResDTO.builder()
                .unitOrderId(unitOrder.getId())
                .orderNumber(unitOrder.getOrderNumber())
                .storeTableName(storeTable.getName())
                .localTime(unitOrder.getRelevantTime())
                .orderMenuList(orderMenuDtos)
                .build();
    }
}