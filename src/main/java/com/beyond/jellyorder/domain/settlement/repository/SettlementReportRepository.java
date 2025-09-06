package com.beyond.jellyorder.domain.settlement.repository;

import com.beyond.jellyorder.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface SettlementReportRepository extends Repository<Sales, UUID> {

    // 상단 카드 합계: opened_at 없으면 paid_at 기준으로 포함
    @Query(value = """
            SELECT COALESCE(SUM(s.total_amount), 0)
              FROM sales s
              JOIN total_order t   ON t.id = s.total_order_id
              JOIN store_table st  ON st.id = t.store_table_id
              LEFT JOIN store_open_close soc ON soc.id = s.store_open_close_id
             WHERE s.status    = 'COMPLETED'
               AND st.store_id = :storeId
               AND COALESCE(soc.opened_at, s.paid_at) >= :from
               AND COALESCE(soc.opened_at, s.paid_at) <  :to
            """, nativeQuery = true)
    long sumGross(@Param("storeId") UUID storeId,
                  @Param("from") LocalDateTime from,
                  @Param("to") LocalDateTime to);

    // 일별: 오픈일 없으면 결제일(paid_at)로 버킷팅
    @Query(value = """
            SELECT DATE(COALESCE(soc.opened_at, s.paid_at)) AS bucket,
                   SUM(COALESCE(s.total_amount, 0))                          AS gross,
                   SUM(COALESCE(s.settlement_amount, s.total_amount, 0))     AS net,
                   SUM(COALESCE(s.total_amount, 0)
                       - COALESCE(s.settlement_amount, s.total_amount, 0))   AS fee,
                   COUNT(*)                                                  AS cnt
              FROM sales s
              JOIN total_order t   ON t.id = s.total_order_id
              JOIN store_table st  ON st.id = t.store_table_id
              LEFT JOIN store_open_close soc ON soc.id = s.store_open_close_id
             WHERE s.status    = 'COMPLETED'
               AND st.store_id = :storeId
               AND COALESCE(soc.opened_at, s.paid_at) >= :from
               AND COALESCE(soc.opened_at, s.paid_at) <  :to
             GROUP BY DATE(COALESCE(soc.opened_at, s.paid_at))
             ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> aggregateDaily(@Param("storeId") UUID storeId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    // 주별: (월요일) 버킷. 오픈일 없으면 paid_at 기준
    @Query(value = """
            SELECT DATE_SUB(
                       DATE(COALESCE(soc.opened_at, s.paid_at)),
                       INTERVAL WEEKDAY(COALESCE(soc.opened_at, s.paid_at)) DAY
                   ) AS bucket_monday,
                   SUM(COALESCE(s.total_amount, 0))                                    AS gross,
                   SUM(COALESCE(s.settlement_amount, s.total_amount, 0))               AS net,
                   SUM(COALESCE(s.total_amount, 0)
                       - COALESCE(s.settlement_amount, s.total_amount, 0))              AS fee,
                   COUNT(*)                                                             AS cnt
              FROM sales s
              JOIN total_order t   ON t.id = s.total_order_id
              JOIN store_table st  ON st.id = t.store_table_id
              LEFT JOIN store_open_close soc ON soc.id = s.store_open_close_id
             WHERE s.status    = 'COMPLETED'
               AND st.store_id = :storeId
               AND COALESCE(soc.opened_at, s.paid_at) >= :from
               AND COALESCE(soc.opened_at, s.paid_at) <  :to
             GROUP BY bucket_monday
             ORDER BY bucket_monday
            """, nativeQuery = true)
    List<Object[]> aggregateWeekly(@Param("storeId") UUID storeId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    // 월별: (해당 월 1일) 버킷. 오픈일 없으면 paid_at 기준
    @Query(value = """
            SELECT DATE_FORMAT(COALESCE(soc.opened_at, s.paid_at), '%Y-%m-01')         AS bucket,
                   SUM(COALESCE(s.total_amount, 0))                                    AS gross,
                   SUM(COALESCE(s.settlement_amount, s.total_amount, 0))               AS net,
                   SUM(COALESCE(s.total_amount, 0)
                       - COALESCE(s.settlement_amount, s.total_amount, 0))             AS fee,
                   COUNT(*)                                                             AS cnt
              FROM sales s
              JOIN total_order t   ON t.id = s.total_order_id
              JOIN store_table st  ON st.id = t.store_table_id
              LEFT JOIN store_open_close soc ON soc.id = s.store_open_close_id
             WHERE s.status    = 'COMPLETED'
               AND st.store_id = :storeId
               AND COALESCE(soc.opened_at, s.paid_at) >= :from
               AND COALESCE(soc.opened_at, s.paid_at) <  :to
             GROUP BY DATE_FORMAT(COALESCE(soc.opened_at, s.paid_at), '%Y-%m-01')
             ORDER BY bucket
            """, nativeQuery = true)
    List<Object[]> aggregateMonthly(@Param("storeId") UUID storeId,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);
}
