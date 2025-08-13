package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderMenuRepository extends JpaRepository<OrderMenu, UUID> {

//    List<OrderMenu> findAllByTotalOrderId(UUID orderId);

    Optional<OrderMenu> findByMenuIdAndUnitOrderId(UUID menuId, UUID unitOrderId);


}
