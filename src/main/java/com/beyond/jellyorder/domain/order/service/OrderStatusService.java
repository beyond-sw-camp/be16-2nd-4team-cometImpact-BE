package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusUpdateReqDTO;
import com.beyond.jellyorder.domain.order.dto.orderStatus.UnitOrderStatusResDTO;
import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import com.beyond.jellyorder.domain.order.repository.UnitOrderRepository;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStatusService {

    private final UnitOrderRepository unitOrderRepository;
    private final StoreJwtClaimUtil storeJwtClaimUtil;
    private final StoreRepository storeRepository;

    // TODO: N+1문제 해결
    @Transactional(readOnly = true)
    public Page<OrderStatusResDTO> getOrderListInOrderStatus(OrderStatus orderStatus, Pageable pageable) {
        // 토큰에서 storeId 추출
        UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 매장이 존재하지 않습니다."));

        Page<UnitOrder> unitOrderPageList = unitOrderRepository.findPageByStoreAndStatusWithin(
                storeId,
                orderStatus,
                store.getBusinessOpenedAt(),
                pageable);
        System.out.println("storeId = " + storeId);
        System.out.println("orderStatus = " + orderStatus);
        System.out.println("store = " + store.getBusinessOpenedAt());

        return unitOrderPageList.map(OrderStatusResDTO::from);
    }

    /**
     * 필수 로직 변환 작업
     * 추후 redis 재고 도입으로 인한 리팩토링 필요함.
     */
    // 접수된 주문 상태변경(주문완료, 취소) 로직
    public UnitOrderStatusResDTO updateUnitOrderStatus(UUID unitOrderId, OrderStatusUpdateReqDTO reqDTO) {
        UnitOrder unitOrder = unitOrderRepository.findById(unitOrderId)
                .orElseThrow(() -> new EntityNotFoundException("해당 단건주문이 존재하지 않습니다."));

        OrderStatus reqOrderStatus = reqDTO.getOrderStatus();

        // 상태 전이 검증 로직 (예: 이미 COMPLETE면 다시 CANCEL 불가)
        if (unitOrder.getStatus() == OrderStatus.COMPLETE || unitOrder.getStatus() == OrderStatus.CANCEL) {
            throw new IllegalStateException("이미 완료되었거나 취소된 주문은 상태를 변경할 수 없습니다.");
        }

        if (reqOrderStatus == OrderStatus.COMPLETE) {
            unitOrder.updateOrderStatus(OrderStatus.COMPLETE);
        } else if (reqOrderStatus == OrderStatus.CANCEL) {
            // 하루 판매 감소
            // TODO: {동시성 제어를 위한 Redis 도입 예정}
            unitOrder.getOrderMenus().forEach(orderMenu -> {
                orderMenu.getMenu().decreaseSalesToday(orderMenu.getQuantity());
            });

            // 상태변경
            unitOrder.updateOrderStatus(OrderStatus.CANCEL);
        }

        return UnitOrderStatusResDTO.from(unitOrder);
    }
}
