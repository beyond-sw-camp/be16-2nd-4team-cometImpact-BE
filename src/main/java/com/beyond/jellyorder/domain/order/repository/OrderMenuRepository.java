package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import org.springframework.data.jpa.repository.EntityGraph;
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



    // totalOrder 기준으로 메뉴, 옵션까지 한 번에 로딩
    @EntityGraph(attributePaths = {"menu", "unitOrder"})
    List<OrderMenu> findAllByUnitOrder_TotalOrder_Id(UUID totalOrderId);

    // totalOrder 조회용
    @Query("""
      select distinct om
      from OrderMenu om
      join om.unitOrder uo
      join uo.totalOrder to
      left join fetch om.orderMenuOptionList omo
      left join fetch omo.subOption so
      left join fetch so.mainOption mo
      left join fetch om.menu m
      where to.id = :totalOrderId
      order by om.id
    """)
    List<OrderMenu> findAllWithOptionsByTotalOrderId(
            @org.springframework.data.repository.query.Param("totalOrderId") UUID totalOrderId
    );

    // 취소된 주문을 제외한 orderMenuList를 fetch join으로 추출.
    @Query("""
      select distinct om
      from OrderMenu om
      join om.unitOrder uo
      join uo.totalOrder to
      left join fetch om.orderMenuOptionList omo
      left join fetch omo.subOption so
      left join fetch so.mainOption mo
      left join fetch om.menu m
      where to.id = :totalOrderId
        and uo.status <> :cancelled
      order by om.id
    """)
    List<OrderMenu> findAllActiveMenusWithOptionsByTotalOrderId(
            @Param("totalOrderId") UUID totalOrderId,
            @Param("cancelled") OrderStatus cancelled
    );

    @Query("""
        select (count(om) > 0)
          from OrderMenu om
          join om.unitOrder uo
          join uo.totalOrder to2
          join to2.storeTable st
         where om.menu.id = :menuId
           and st.status = com.beyond.jellyorder.domain.storetable.entity.TableStatus.EATING
        """)
    boolean existsInEatingTable(@Param("menuId") UUID menuId);
}
