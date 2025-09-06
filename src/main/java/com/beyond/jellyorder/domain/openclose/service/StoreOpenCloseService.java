package com.beyond.jellyorder.domain.openclose.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.domain.openclose.dto.CloseSummaryDTO;
import com.beyond.jellyorder.domain.openclose.dto.OpenSummaryDTO;
import com.beyond.jellyorder.domain.openclose.entity.StoreOpenClose;
import com.beyond.jellyorder.domain.openclose.repository.StoreOpenCloseRepository;
import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.service.CollectOrderNumberService;
import com.beyond.jellyorder.domain.order.service.RdbOrderNumberService;
import com.beyond.jellyorder.domain.sales.dto.SalesSummaryDTO;
import com.beyond.jellyorder.domain.sales.entity.SalesStatus;
import com.beyond.jellyorder.domain.sales.repository.SalesRepository;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreOpenCloseService {

    private final StoreOpenCloseRepository storeOpenCloseRepository;
    private final StoreTableRepository storeTableRepository;
    private final RdbOrderNumberService rdbOrderNumberService;
    private final CollectOrderNumberService collectOrderNumberService;
    private final SalesRepository salesRepository;
    private final StoreRepository storeRepository;
    private final StoreJwtClaimUtil claimUtil;

    @PersistenceContext private EntityManager em;

    /** 마감 + 요약 DTO 반환 */
    public CloseSummaryDTO close(LocalDateTime closedAtNullable) {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());

        // 1) 현재 오픈 세션을 비관적 락으로 조회
        StoreOpenClose current = em.createQuery(
                        "select s from StoreOpenClose s where s.store.id = :sid and s.closedAt is null",
                        StoreOpenClose.class)
                .setParameter("sid", storeId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream().findFirst()
                .orElseThrow(() -> new IllegalStateException("현재 영업 중이 아닙니다."));
        long pendingCnt = salesRepository.countBySessionAndStatus(current.getId(), SalesStatus.PENDING);
        if (pendingCnt > 0) {
            throw new IllegalStateException("미결제(PENDING) 결제건이 있어 마감할 수 없습니다.");
        }


        // 2) 진행 중 주문이 있으면 마감 차단
        boolean hasActive = storeTableRepository.existsOpenOrderInStore(storeId, OrderStatus.CANCEL);
        if (hasActive) {
            throw new IllegalStateException("진행 중 주문이 있어 마감할 수 없습니다.");
        }

        // 3) 모든 테이블 STANDBY로
        List<StoreTable> tables = storeTableRepository.findAllByStoreId(storeId);
        tables.forEach(t -> t.changeStatus(TableStatus.STANDBY));

        // 4) 주문번호 초기화 (RDB + Redis)
        rdbOrderNumberService.reset(storeId);
        collectOrderNumberService.resetOrderNum(storeId);

        // 5) 마감 시간 확정
        LocalDateTime closedAt = (closedAtNullable != null) ? closedAtNullable : LocalDateTime.now();
        current.storeClose(closedAt);

        // 6) 이 세션 매출 요약 집계 (COMPLETED만)
        SalesSummaryDTO sum = salesRepository.summarizeByOpenClose(storeId, current.getId(), SalesStatus.COMPLETED);
        long gross = (sum != null && sum.getGross() != null) ? sum.getGross() : 0L;
        long cnt   = (sum != null && sum.getCnt()   != null) ? sum.getCnt()   : 0L;

        // 7) 매장 정보
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("매장을 찾을 수 없습니다. " + storeId));

        // 8) DTO 조립 후 반환
        return CloseSummaryDTO.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .openedAt(current.getOpenedAt())
                .closedAt(current.getClosedAt())
                .receiptCount(cnt)
                .grossAmount(gross)
                .build();

    }

    /** 현재 오픈 상태 확인 */
    @Transactional(readOnly = true)
    public StoreOpenClose getOpenOrThrow() {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());
        return storeOpenCloseRepository.findOpen(storeId)
                .orElseThrow(() -> new IllegalStateException("현재 영업 중이 아닙니다."));
    }

    @Transactional
    public OpenSummaryDTO open(LocalDateTime openedAtNullable) {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());

        // 1) 중복 오픈 방지
        storeOpenCloseRepository.findOpen(storeId).ifPresent(s -> {
            throw new IllegalStateException("이미 영업 중입니다. 영업 오픈 시각 : " + s.getOpenedAt());
        });

        // 2) 매장 락(or 보통 조회) 후 세션 생성
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("매장을 찾을 수 없습니다. " + storeId));

        LocalDateTime openedAt = (openedAtNullable != null) ? openedAtNullable : LocalDateTime.now();

        StoreOpenClose oc = storeOpenCloseRepository.save(
                StoreOpenClose.builder()
                        .store(store)
                        .openedAt(openedAt)
                        .build()
        );

        // 3) 대시보드/주문현황 기준 시각 갱신 (OrderStatusService에서 사용)
        // Store 엔티티에 세터가 없다면 작은 메서드 하나 추가해줘: changeBusinessOpenedAt(...)
        store.changeBusinessOpenedAt(openedAt);

        return OpenSummaryDTO.builder()
                .openCloseId(oc.getId())
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .openedAt(openedAt)
                .build();
    }

}
