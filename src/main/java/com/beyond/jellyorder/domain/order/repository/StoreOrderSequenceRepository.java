package com.beyond.jellyorder.domain.order.repository;

import com.beyond.jellyorder.domain.order.entity.StoreOrderSequence;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StoreOrderSequenceRepository extends JpaRepository<StoreOrderSequence, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StoreOrderSequence s where s.store.id = :storeId")
    Optional<StoreOrderSequence> findByStoreIdForUpdate(@Param("storeId") UUID storeId);
    Optional<StoreOrderSequence> findByStoreId(UUID storeId);
}
