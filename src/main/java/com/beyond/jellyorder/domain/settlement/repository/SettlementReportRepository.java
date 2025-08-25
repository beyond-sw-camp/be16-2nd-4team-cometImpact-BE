package com.beyond.jellyorder.domain.settlement.repository;

import com.beyond.jellyorder.domain.sales.entity.Sales;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface SettlementReportRepository extends Repository<Sales, UUID> {

    // 일별
    @Query(value = """
SELECT DATE(s.paid_at) AS bucket,
       SUM(COALESCE(s.total_amount, 0)) AS gross,
       SUM(COALESCE(s.settlement_amount, s.total_amount, 0)) AS net,
       SUM(COALESCE(s.total_amount, 0) - COALESCE(s.settlement_amount, s.total_amount, 0)) AS fee,
       COUNT(*) AS cnt
  FROM sales s
 WHERE s.status = 'COMPLETED'
   AND s.paid_at >= :from AND s.paid_at < :to
 GROUP BY DATE(s.paid_at)
 ORDER BY bucket
""", nativeQuery = true)
    List<Object[]> aggregateDaily(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 주별(ISO, 월요일 시작)
    @Query(value = """
SELECT YEARWEEK(s.paid_at, 1) AS bucket_key,
       DATE_SUB(DATE(s.paid_at), INTERVAL WEEKDAY(s.paid_at) DAY) AS bucket,
       SUM(COALESCE(s.total_amount, 0)) AS gross,
       SUM(COALESCE(s.settlement_amount, s.total_amount, 0)) AS net,
       SUM(COALESCE(s.total_amount, 0) - COALESCE(s.settlement_amount, s.total_amount, 0)) AS fee,
       COUNT(*) AS cnt
  FROM sales s
 WHERE s.status = 'COMPLETED'
   AND s.paid_at >= :from AND s.paid_at < :to
 GROUP BY bucket_key, bucket
 ORDER BY bucket
""", nativeQuery = true)
    List<Object[]> aggregateWeekly(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);


    // 월별
    @Query(value = """
SELECT DATE_FORMAT(s.paid_at, '%Y-%m-01') AS bucket,
       SUM(COALESCE(s.total_amount, 0)) AS gross,
       SUM(COALESCE(s.settlement_amount, s.total_amount, 0)) AS net,
       SUM(COALESCE(s.total_amount, 0) - COALESCE(s.settlement_amount, s.total_amount, 0)) AS fee,
       COUNT(*) AS cnt
  FROM sales s
 WHERE s.status = 'COMPLETED'
   AND s.paid_at >= :from AND s.paid_at < :to
 GROUP BY DATE_FORMAT(s.paid_at, '%Y-%m-01')
 ORDER BY bucket
""", nativeQuery = true)
    List<Object[]> aggregateMonthly(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
