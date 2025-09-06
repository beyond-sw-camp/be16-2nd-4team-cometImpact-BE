package com.beyond.jellyorder.domain.openclose.repository;

import com.beyond.jellyorder.domain.openclose.entity.StoreOpenClose;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface StoreOpenCloseRepository extends JpaRepository<StoreOpenClose, UUID> {
    @Query("select s from StoreOpenClose s where s.store.id = :storeId and s.closedAt is null")
    Optional<StoreOpenClose> findOpen(@Param("storeId") UUID storeId);

    @Query("""
       select s from StoreOpenClose s
        where s.store.id = :storeId
          and s.openedAt <= :ts
          and (s.closedAt is null or :ts < s.closedAt)
    """)
    Optional<StoreOpenClose> findAt(@Param("storeId") UUID storeId, @Param("ts") LocalDateTime ts);

    @Query("""
       select s from StoreOpenClose s
        where s.store.id = :storeId and s.openedAt <= :ts
        order by s.openedAt desc
    """)
    Optional<StoreOpenClose> findLastBefore(@Param("storeId") UUID storeId, @Param("ts") LocalDateTime ts);

}
