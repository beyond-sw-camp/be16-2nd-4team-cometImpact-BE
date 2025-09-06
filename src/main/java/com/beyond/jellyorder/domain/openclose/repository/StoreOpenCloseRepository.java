package com.beyond.jellyorder.domain.openclose.repository;

import com.beyond.jellyorder.domain.openclose.entity.StoreOpenClose;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface StoreOpenCloseRepository extends JpaRepository<StoreOpenClose, UUID> {
    // 현재 열린 세션(락 없음) — 조회/표시용
    @Query("select s from StoreOpenClose s where s.store.id = :storeId and s.closedAt is null")
    Optional<StoreOpenClose> findOpen(@Param("storeId") UUID storeId);

    // ✅ 현재 열린 세션(비관적 읽기 락) — 주문/결제 진입부에서 사용
    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")) // 3초 대기(선택)
    @Query("select s from StoreOpenClose s where s.store.id = :storeId and s.closedAt is null")
    Optional<StoreOpenClose> findOpenForUpdate(@Param("storeId") UUID storeId);

    // 특정 시각(ts)에 유효한 세션 찾기 — 과거/경계 보정용
    @Query("""
       select s from StoreOpenClose s
        where s.store.id = :storeId
          and s.openedAt <= :ts
          and (s.closedAt is null or :ts < s.closedAt)
    """)
    Optional<StoreOpenClose> findAt(@Param("storeId") UUID storeId, @Param("ts") LocalDateTime ts);

    // ts 이전의 마지막 세션 — 경계에 딱 안 걸릴 때 보정용
    @Query("""
       select s from StoreOpenClose s
        where s.store.id = :storeId and s.openedAt <= :ts
        order by s.openedAt desc
    """)
    Optional<StoreOpenClose> findLastBefore(@Param("storeId") UUID storeId, @Param("ts") LocalDateTime ts);
}
