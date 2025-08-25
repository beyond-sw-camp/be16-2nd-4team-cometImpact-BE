package com.beyond.jellyorder.domain.settlement.repository;

import com.beyond.jellyorder.domain.sales.entity.Sales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
public interface SettlementDetailRepository extends Repository<Sales, java.util.UUID>{
    @Query(value = """
        SELECT DATE_FORMAT(s.paid_at, '%Y-%m-%d %H:%i:%s') AS paidDate,
               s.payment_method                           AS paymentMethod,
               om.menu_name                               AS menuName,
               om.menu_price                              AS menuPrice,
               om.quantity                                AS quantity,
               omo.option_name                            AS optionName,
               omo.option_price                           AS optionPrice,
               ((om.menu_price + COALESCE(omo_sum.sum_opt_price, 0)) * om.quantity) AS lineTotal
          FROM sales s
          JOIN total_order t        ON t.id = s.total_order_id
          JOIN unit_order uo        ON uo.total_order_id = t.id
          JOIN order_menu om        ON om.unit_order_id  = uo.id
     LEFT JOIN order_menu_option omo ON omo.order_menu_id = om.id
     LEFT JOIN (
            SELECT omo2.order_menu_id, SUM(COALESCE(omo2.option_price,0)) AS sum_opt_price
              FROM order_menu_option omo2
             GROUP BY omo2.order_menu_id
        ) omo_sum ON omo_sum.order_menu_id = om.id
         WHERE s.status = 'COMPLETED'
           AND s.paid_at >= :from AND s.paid_at < :to
         ORDER BY s.paid_at, om.id
        """,
            countQuery = """
        SELECT COUNT(*)
          FROM sales s
          JOIN total_order t        ON t.id = s.order_id
          JOIN unit_order uo        ON uo.total_order_id = t.id
          JOIN order_menu om        ON om.unit_order_id  = uo.id
     LEFT JOIN order_menu_option omo ON omo.order_menu_id = om.id
         WHERE s.status = 'COMPLETED'
           AND s.paid_at >= :from AND s.paid_at < :to
        """,
            nativeQuery = true)
    Page<Object[]> findLineItems(@Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to,
                                 Pageable pageable);

}
