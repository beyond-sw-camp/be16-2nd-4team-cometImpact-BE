package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import com.beyond.jellyorder.domain.order.repository.UnitOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStatusService {

    private final UnitOrderRepository unitOrderRepository;

    @Transactional(readOnly = true)
    public Page<OrderStatusResDTO> getOrderListInOrderStatus(OrderStatus orderStatus, Pageable pageable) {
        Page<UnitOrder> unitOrderPageList = unitOrderRepository.findByStatus(orderStatus, pageable);

        return unitOrderPageList.map(OrderStatusResDTO::from);
    }
}
