package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderMenuRepository extends JpaRepository<OrderMenu, UUID> {

    Optional<OrderMenu> findByMenuIdAndUnitOrderId(UUID menuId, UUID unitOrderId);

//    @Query("""
//    select distinct om
//    from OrderMenu om
//    left join fetch om.orderMenuOptions omo
//    where om.unitOrder.id = :unitOrderId
//""")
//    List<OrderMenu> findOrderMenusWithOptionsByUnitOrderId(@Param("unitOrderId") UUID unitOrderId);

    List<OrderMenu> findAllByUnitOrderId(UUID unitOrderId);





}
