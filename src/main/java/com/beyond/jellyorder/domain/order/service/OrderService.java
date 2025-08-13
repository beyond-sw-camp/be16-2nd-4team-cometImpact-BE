package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.order.dto.UnitOrderCreateReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderMenuReqDto;
import com.beyond.jellyorder.domain.order.dto.UnitOrderResDto;
import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import com.beyond.jellyorder.domain.order.repository.OrderMenuRepository;
import com.beyond.jellyorder.domain.order.repository.TotalOrderRepository;
import com.beyond.jellyorder.domain.order.repository.UnitOrderRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final UnitOrderRepository unitOrderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final TotalOrderRepository totalOrderRepository;
    private final StoreTableRepository storeTableRepository;
    private final MenuRepository menuRepository;
    // pub/sub 기능 위해 Redis 필요

    /**
     * 단위주문 생성
     * 해당 테이블의 최신 전체주문을 찾고, 종료되지 않았다면 재사용
     * 없거나 이미 종료(endedAt != null)면 새 전체주문 생성
     * 요청 내 각 메뉴 수량 >= 1
     * 총 금액/수량 집계 후 TotalOrder에 누적
     */
    public UnitOrderResDto createUnit(UnitOrderCreateReqDto dto) {
        /* 테이블 조회 */
        StoreTable storeTable = storeTableRepository.findById(dto.getStoreTableId())
                .orElseThrow(() -> new EntityNotFoundException("해당 매장의 테이블이 없습니다."));

        /* 종료되지 않은 주문 확보 */
        TotalOrder totalOrder = totalOrderRepository.findTopByStoreTableOrderByOrderedAtDesc(storeTable)
                .filter(to -> to.getEndedAt() == null)  // 종료되지 않은 주문만 재사용
                .orElseGet(() -> totalOrderRepository.save(
                        TotalOrder.builder()
                                .storeTable(storeTable)
                                .totalPrice(0)  // 새로 시작하는 전체 주문의 초기값
                                .count(0)       // 새로 시작하는 전체 주문의 초기값
                                .build()
                ));

        /* 단위주문 전송 시 합계/수량 */
        int unitPrice = 0;      // 단위주문의 합계 금액
        int unitCount = 0;      // 단위주문의 총 수량

        /* 단위주문 생성 & 저장 */
        UnitOrder unitOrder = UnitOrder.builder()
                .totalOrder(totalOrder)
                .status(OrderStatus.ACCEPT) // 전송 직후 상태는 ACCEPT
                .totalCount(0)
                .build();
        unitOrderRepository.save(unitOrder);

        /* OrderMenu 저장 */
        for (UnitOrderMenuReqDto menuReqDto : dto.getMenus()) {
            if (menuReqDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
            }
            Menu menu = menuRepository.findById(menuReqDto.getMenuId())
                    .orElseThrow(() -> new EntityNotFoundException("해당 메뉴가 존재하지 않습니다."));

            /* 단위주문 금액, 수량 계산 */
            Integer menuPrice = menu.getPrice() * menuReqDto.getQuantity();
            unitPrice += menuPrice;
            unitCount += menuReqDto.getQuantity();


            OrderMenu orderMenu = OrderMenu.builder()
                    .unitOrder(unitOrder)
                    .menu(menu)
                    .quantity(menuReqDto.getQuantity())
                    .build();
            orderMenuRepository.save(orderMenu);
        }

        /* 집계 반영 */
        unitOrder.setUnitCount(unitCount);
        totalOrder.addUnitTotal(unitPrice, unitCount);

        return UnitOrderResDto.builder()
                        .totalOrderId(totalOrder.getId())
                        .unitOrderId(unitOrder.getId())
                        .unitPrice(unitPrice)
                        .unitCount(unitCount)
                        .build();
    }
}
