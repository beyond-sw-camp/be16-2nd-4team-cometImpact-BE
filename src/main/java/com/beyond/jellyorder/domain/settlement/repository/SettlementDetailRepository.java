package com.beyond.jellyorder.domain.settlement.repository;

import com.beyond.jellyorder.domain.sales.entity.Sales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.*;

import java.time.LocalDateTime;

public interface SettlementDetailRepository extends Repository<Sales, java.util.UUID> {
    // 단위주문 헤더(결제시간/결제수단/상태/총액) 페이지 조회
    @Query(value = """
        SELECT 
            s.id                                                     AS receiptId,       -- 결제건 ID
            DATE_FORMAT(COALESCE(s.paid_at, s.updated_at), '%Y-%m-%d %H:%i:%s') AS paidDate,
            s.payment_method                                          AS paymentMethod,
            s.status                                                  AS status,
            SUM( (om.menu_price + COALESCE(ops.sum_opt_price, 0)) * om.quantity ) AS totalAmount
        FROM sales s
        JOIN total_order t   ON s.total_order_id = t.id
        JOIN store_table st  ON t.store_table_id = st.id
        JOIN store ss        ON st.store_id = ss.id
        JOIN unit_order uo   ON uo.total_order_id = t.id
        JOIN order_menu om   ON om.unit_order_id = uo.id
        LEFT JOIN (
            SELECT omo.order_menu_id, SUM(COALESCE(omo.option_price,0)) AS sum_opt_price
            FROM order_menu_option omo
            GROUP BY omo.order_menu_id
        ) ops ON ops.order_menu_id = om.id
        WHERE ss.id = :storeId
          AND COALESCE(s.paid_at, s.updated_at) >= :from AND COALESCE(s.paid_at, s.updated_at) < :to
          AND (:status IS NULL OR s.status = :status)
        GROUP BY s.id, COALESCE(s.paid_at, s.updated_at), s.payment_method, s.status
        ORDER BY COALESCE(s.paid_at, s.updated_at) DESC, s.id
        """,
            countQuery = """
        SELECT COUNT(*) FROM (
            SELECT s.id
            FROM sales s
            JOIN total_order t  ON s.total_order_id = t.id
            JOIN store_table st ON t.store_table_id = st.id
            JOIN store ss       ON st.store_id = ss.id
            WHERE ss.id = :storeId
              AND COALESCE(s.paid_at, s.updated_at) >= :from AND COALESCE(s.paid_at, s.updated_at) < :to
              AND (:status IS NULL OR s.status = :status)
            GROUP BY s.id
        ) x
        """,
            nativeQuery = true)
    Page<Object[]> findReceiptHeaders(
            @Param("storeId") UUID storeId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") String status,
            Pageable pageable
    );

    // 메뉴/옵션 상세(페이지에 포함된 unitOrderId들에 대해 한 번에 조회)
    @Query(value = """
        SELECT
            s.id               AS receiptId,
            om.id              AS orderMenuId,
            om.menu_name       AS menuName,
            om.menu_price      AS menuPrice,
            om.quantity        AS quantity,
            omo.option_name    AS optionName,
            omo.option_price   AS optionPrice
        FROM sales s
        JOIN total_order t       ON s.total_order_id = t.id
        JOIN unit_order uo       ON uo.total_order_id = t.id
        JOIN order_menu om       ON om.unit_order_id = uo.id
        LEFT JOIN order_menu_option omo ON omo.order_menu_id = om.id
        WHERE s.id IN (:receiptIds)
        ORDER BY s.id, om.id
        """,
            nativeQuery = true)
    List<Object[]> findMenuLinesByReceipts(@Param("receiptIds") List<UUID> receiptIds);
}
