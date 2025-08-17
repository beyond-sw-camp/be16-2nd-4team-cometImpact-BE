package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.option.dto.MainOptionReqDto;
import com.beyond.jellyorder.domain.option.dto.SubOptionReqDto;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import com.beyond.jellyorder.domain.option.subOption.repository.SubOptionRepository;
import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderMenuReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
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
@Transactional
@RequiredArgsConstructor
public class UnitOrderService {

    private final UnitOrderRepository unitOrderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final TotalOrderRepository totalOrderRepository;
    private final StoreTableRepository storeTableRepository;
    private final MenuRepository menuRepository;
    private final SubOptionRepository subOptionRepository;

    /**
     * 단위주문 생성
     * 해당 테이블의 최신 전체주문을 찾고, 종료되지 않았다면 재사용
     * 없거나 이미 종료(endedAt != null)면 새 전체주문 생성
     * 요청 내 각 메뉴 수량 >= 1
     * 총 금액/수량 집계 후 TotalOrder에 누적
     */
    public UnitOrderResDto createUnit(UnitOrderCreateReqDto dto) {
        /* 요청값 검증 */
        if (dto.getMenus() == null || dto.getMenus().isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }

        /* 수량 검증 */
        dto.getMenus().forEach(menuReqDto -> {
            if (menuReqDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("수량은 1개 이상이어야 합니다. 메뉴ID : " + menuReqDto.getMenuId());
            }
        });

        /* 테이블 조회 */
        StoreTable storeTable = storeTableRepository.findById(dto.getStoreTableId())
                .orElseThrow(() -> new EntityNotFoundException("해당 매장의 테이블이 없습니다."));

        /* 종료되지 않은 주문 확보 */
        boolean createdNewOrder = false;

        Optional<TotalOrder> latestOrder =
                totalOrderRepository.findTopByStoreTableOrderByOrderedAtDesc(storeTable);

        TotalOrder totalOrder;
        if (latestOrder.isPresent() && latestOrder.get().getEndedAt() == null) {
            // 미종료 주문 재사용
            totalOrder = latestOrder.get();
        } else {
            // 신규 전체주문 생성
            totalOrder = totalOrderRepository.save(
                    TotalOrder.builder()
                            .storeTable(storeTable)
                            .totalPrice(0)
                            .count(0)
                            .build()
            );
            createdNewOrder = true;
        }

        /* 단위주문 전송 시 합계/수량 */
        int unitPrice = 0;      // 단위주문의 합계 금액
        int unitCount = 0;      // 단위주문의 총 수량

        /* 단위주문 생성 */
        UnitOrder unitOrder = UnitOrder.builder()
                .totalOrder(totalOrder)
                .status(OrderStatus.ACCEPT) // 전송 직후 상태는 ACCEPT
                .totalCount(0)
                .build();
        unitOrderRepository.save(unitOrder);

        /* 단위주문 저장 */
        for (UnitOrderMenuReqDto menuReqDto : dto.getMenus()) {
            Menu menu = menuRepository.findById(menuReqDto.getMenuId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 메뉴가 존재하지 않습니다."));
            int stockQuantity = menu.getSalesLimit() - menu.getSalesToday();

            // 품절여부 확인
            if (menu.isSoldOut()) {
                throw new IllegalArgumentException("품절된 상품입니다.");
            }

            // -1이 아닐 경우 한정판매
            if (menu.getSalesLimit() != -1) {
                if (stockQuantity < menuReqDto.getQuantity()) {
                    throw new IllegalArgumentException("현재 남은 수량은 " + stockQuantity + "개 입니다.");
                } else {
                    menu.increaseSalesToday(menuReqDto.getQuantity());
                    if (menu.getSalesLimit() == menu.getSalesToday()) {
                        menu.setSoldOut(true);
                    }
                }
            } else {    // -1일 경우 상시판매
                menu.increaseSalesToday(menuReqDto.getQuantity());
            }

            /* 단위주문 금액, 수량 계산 */
            int menuPrice = menu.getPrice() * menuReqDto.getQuantity(); // 메뉴 가격
            int optionsPriceTotal = 0;  // 메뉴의 옵션 총 가격

            // OrderMenu 저장
            OrderMenu orderMenu = OrderMenu.builder()
                    .unitOrder(unitOrder)
                    .menu(menu)
                    .quantity(menuReqDto.getQuantity())
                    .build();
            orderMenuRepository.save(orderMenu);

            // 옵션 모으기
            Map<UUID, Integer> quantityBySubId = new LinkedHashMap<>();
            Map<UUID, Integer> priceBySubId = new LinkedHashMap<>();

            // 옵션 체크 (메인옵션, 서브옵션)
            if (menuReqDto.getMainOptions() != null && !menuReqDto.getMainOptions().isEmpty()) {
                for (MainOptionReqDto mainDto : menuReqDto.getMainOptions()) {
                    if (mainDto.getSubOptions() == null || mainDto.getSubOptions().isEmpty()) {
                        continue;
                    }

                    for (SubOptionReqDto subDto : mainDto.getSubOptions()) {
                        UUID subId = UUID.fromString(subDto.getSubOptionId());
                        quantityBySubId.merge(subId, subDto.getQuantity(), Integer::sum);
                        priceBySubId.put(subId, subDto.getPrice());
                    }
                }

                // DB에서 서브옵션 조회
                List<SubOption> subOptions = subOptionRepository.findAllById(quantityBySubId.keySet());

                // 서브옵션이 해당 메뉴의 메인옵션에 속해있는지 검증
                for (SubOption so : subOptions) {
                    if (so.getMainOption() == null || so.getMainOption().getMenu() == null ||
                            !so.getMainOption().getMenu().getId().equals(menu.getId())) {
                        throw new IllegalArgumentException("선택한 옵션이 해당 메뉴의 옵션이 아닙니다.");
                    }

                    // 옵션 합산값 저장
                    int quantity = quantityBySubId.getOrDefault(so.getId(), 0);
                    optionsPriceTotal += so.getPrice() * quantity;
                }

                // 옵션 저장
                for (SubOption so : subOptions) {
                    int quantity = quantityBySubId.getOrDefault(so.getId(), 0);

                    if (quantity == 0) {
                        continue;
                    }

                    OrderMenuOption orderMenuOption = OrderMenuOption.builder()
                            .orderMenu(orderMenu)
                            .subOption(so)
                            .optionName(so.getName())
                            .optionPrice(so.getPrice())
                            .optionQuantity(quantity)
                            .build();
                    // cascade : 부모에 add
                    orderMenu.getOrderMenuOptionList().add(orderMenuOption);
                }
            }

            unitPrice += menuPrice + optionsPriceTotal;
            unitCount += menuReqDto.getQuantity();

        }

        /* 집계 반영 */
        unitOrder.updateUnitCount(unitCount);
        totalOrder.addUnitTotal(unitPrice, unitCount);

        /* 해당 테이블 상태 변환 : 새 전체주문일 때 */
        if (createdNewOrder && storeTable.getStatus() != TableStatus.EATING) {
            storeTable.changeStatus(TableStatus.EATING);
        }

        return UnitOrderResDto.builder()
                        .totalOrderId(totalOrder.getId())
                        .unitOrderId(unitOrder.getId())
                        .unitPrice(unitPrice)
                        .unitCount(unitCount)
                        .build();
    }
}
