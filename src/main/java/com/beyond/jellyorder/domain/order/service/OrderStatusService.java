package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
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
}
