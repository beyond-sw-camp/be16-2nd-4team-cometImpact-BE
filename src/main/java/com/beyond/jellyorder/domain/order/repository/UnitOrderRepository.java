package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnitOrderRepository extends JpaRepository<UnitOrder, UUID> {

    // 해당 인자값의 status가 아닌 totalOrder에 속한 unitOrder리스트 추출 메서드.
    List<UnitOrder> findAllByTotalOrderIdAndStatusNot(UUID totalOrderId, OrderStatus status);

}
