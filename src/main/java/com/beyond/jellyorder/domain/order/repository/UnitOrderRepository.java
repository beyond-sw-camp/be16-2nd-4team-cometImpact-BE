package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UnitOrderRepository extends JpaRepository<UnitOrder, UUID> {

    // 해당 인자값의 status가 아닌 totalOrder에 속한 unitOrder리스트 추출 메서드.
    List<UnitOrder> findAllByTotalOrderIdAndStatusNot(UUID totalOrderId, OrderStatus status);

    Page<UnitOrder> findByStatus(OrderStatus orderStatus, Pageable pageable);

    // 상태 + 매장 + 시간 구간 + 페이징
    @Query("""
        select u
        from UnitOrder u
        join u.totalOrder t
        join t.storeTable st
        join st.store s
        where s.id = :storeId
          and u.status = :status
          and u.acceptedAt >= :startAt
        """)
    Page<UnitOrder> findPageByStoreAndStatusWithin(
            @Param("storeId") UUID storeId,
            @Param("status") OrderStatus status,
            @Param("startAt") LocalDateTime startAt,
            Pageable pageable
    );
}
