package com.beyond.jellyorder.domain.openclose.service;
import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.domain.openclose.entity.StoreOpenClose;
import com.beyond.jellyorder.domain.openclose.repository.StoreOpenCloseRepository;
import com.beyond.jellyorder.domain.order.service.RdbOrderNumberService;
import com.beyond.jellyorder.domain.order.service.CollectOrderNumberService;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreOpenCloseService {
    private final StoreOpenCloseRepository storeOpenCloseRepository;
    private final StoreTableRepository storeTableRepository;
    private final RdbOrderNumberService rdbOrderNumberService;
    private final CollectOrderNumberService collectOrderNumberService;
    private final StoreJwtClaimUtil claimUtil;

    @PersistenceContext private EntityManager em;

    /** 마감 */
    public void close(LocalDateTime closedAtNullable) {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());

        StoreOpenClose current = em.createQuery(
                        "select s from StoreOpenClose s where s.store.id = :sid and s.closedAt is null",
                        StoreOpenClose.class)
                .setParameter("sid", storeId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream().findFirst()
                .orElseThrow(() -> new IllegalStateException("현재 영업 중이 아닙니다."));

        // 진행 중 주문 차단
        boolean hasActive = storeTableRepository.existsOpenOrderInStore(storeId, com.beyond.jellyorder.domain.order.entity.OrderStatus.CANCEL);
        if (hasActive) throw new IllegalStateException("진행 중 주문이 있어 마감할 수 없습니다.");

        // 모든 테이블 STANDBY
        List<StoreTable> tables = storeTableRepository.findAllByStoreId(storeId);
        tables.forEach(t -> t.changeStatus(TableStatus.STANDBY));

        // 주문번호 초기화 (RDB + Redis)
        rdbOrderNumberService.reset(storeId);
        collectOrderNumberService.resetOrderNum(storeId);

        // 마감 시간
        current.storeClose(closedAtNullable != null ? closedAtNullable : LocalDateTime.now());
    }

    /** 현재 오픈 상태 확인용 */
    @Transactional(readOnly = true)
    public StoreOpenClose getOpenOrThrow() {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());
        return storeOpenCloseRepository.findOpen(storeId).orElseThrow(() -> new IllegalStateException("현재 영업 중이 아닙니다."));
    }
}
