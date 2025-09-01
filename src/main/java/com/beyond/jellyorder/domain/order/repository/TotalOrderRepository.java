package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.TotalOrder;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TotalOrderRepository extends JpaRepository<TotalOrder, UUID> {

    Optional<TotalOrder> findTopByStoreTableOrderByOrderedAtDesc(StoreTable table);

    // "활성 주문(미종료)만" 가져오기
    Optional<TotalOrder> findTopByStoreTableAndEndedAtIsNullOrderByOrderedAtDesc(StoreTable table);


}
