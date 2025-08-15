package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderMenuRepository extends JpaRepository<OrderMenu, UUID> {

    Optional<OrderMenu> findByMenuIdAndUnitOrderId(UUID menuId, UUID unitOrderId);

    @Query("""
    select distinct om
    from OrderMenu om
    join fetch om.menu
    left join fetch om.orderMenuOptionList omo
    left join fetch omo.subOption so
    where om.unitOrder.id = :unitOrderId
""")
    List<OrderMenu> findAllByUnitOrderIdWithOptions(@Param("unitOrderId") UUID unitOrderId);


    List<OrderMenu> findAllByUnitOrderId(UUID unitOrderId);





}
