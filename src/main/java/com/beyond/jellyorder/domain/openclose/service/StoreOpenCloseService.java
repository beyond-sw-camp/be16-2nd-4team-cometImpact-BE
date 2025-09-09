package com.beyond.jellyorder.domain.openclose.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.domain.openclose.dto.CloseSummaryDTO;
import com.beyond.jellyorder.domain.openclose.dto.OpenCloseStatusDTO;
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

import java.time.Clock;
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
    private final Clock clock;

    @PersistenceContext
    private EntityManager em;

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    /**
     * 오픈
     */
    public OpenSummaryDTO open() {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());

        storeOpenCloseRepository.findOpen(storeId).ifPresent(s -> {
            throw new IllegalStateException("이미 영업 중입니다. 영업 오픈 시각 : " + s.getOpenedAt());
        });

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("매장을 찾을 수 없습니다. " + storeId));

        LocalDateTime openedAt = now();

        StoreOpenClose oc = storeOpenCloseRepository.save(
                StoreOpenClose.builder()
                        .store(store)
                        .openedAt(openedAt)
                        .build()
        );

        // "오늘" 기준 갱신(주문 현황 필터에 사용)
        store.changeBusinessOpenedAt(openedAt);
        store.changeBusinessClosedAt(null);

        return OpenSummaryDTO.builder()
                .openCloseId(oc.getId())
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .openedAt(openedAt)
                .build();
    }

    /**
     * 마감 + 요약 DTO
     */
    public CloseSummaryDTO close() {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());

        StoreOpenClose current = em.createQuery(
                        "select s from StoreOpenClose s where s.store.id = :sid and s.closedAt is null",
                        StoreOpenClose.class)
                .setParameter("sid", storeId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream().findFirst()
                .orElseThrow(() -> new IllegalStateException("현재 영업 중이 아닙니다."));

        // (선택) 세션 내 PENDING 결제건 차단
        long pendingCnt = salesRepository.countBySessionAndStatus(current.getId(), SalesStatus.PENDING);
        if (pendingCnt > 0) throw new IllegalStateException("미결제(PENDING) 결제건이 있어 마감할 수 없습니다.");

        // 진행 중 주문 차단
        boolean hasActive = storeTableRepository.existsOpenOrderInStore(storeId);
        if (hasActive) throw new IllegalStateException("진행 중 주문이 있어 마감할 수 없습니다.");

        // 테이블 초기화
        storeTableRepository.findAllByStoreId(storeId)
                .forEach(t -> t.changeStatus(TableStatus.STANDBY));

        // 주문번호 초기화
        rdbOrderNumberService.reset(storeId);
        collectOrderNumberService.resetOrderNum(storeId);

        // 서버 시간으로 마감
        LocalDateTime closedAt = now();
        current.storeClose(closedAt);

        // 매출 요약(세션 기준)
        SalesSummaryDTO sum = salesRepository.summarizeByOpenClose(
                storeId, current.getId(), SalesStatus.COMPLETED);

        long gross = (sum != null && sum.getGross() != null) ? sum.getGross() : 0L;
        long cnt = (sum != null && sum.getCnt() != null) ? sum.getCnt() : 0L;

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("매장을 찾을 수 없습니다. " + storeId));

        return CloseSummaryDTO.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .openedAt(current.getOpenedAt())
                .closedAt(current.getClosedAt())
                .receiptCount(cnt)
                .grossAmount(gross)
                .build();
    }

    @Transactional(readOnly = true)
    public OpenCloseStatusDTO status() {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());

        // 현재 열려있는 세션
        return storeOpenCloseRepository.findOpen(storeId)
                .map(oc -> OpenCloseStatusDTO.open(
                        oc.getId(),
                        oc.getStore().getId(),
                        oc.getStore().getStoreName(),
                        oc.getOpenedAt()
                ))
                .orElseGet(() -> {
                    // 마지막 세션(마감된 것 포함)
                    StoreOpenClose last = storeOpenCloseRepository
                            .findTopByStoreIdOrderByOpenedAtDesc(storeId)
                            .orElse(null);

                    CloseSummaryDTO summary = null;
                    if (last != null && last.getClosedAt() != null) {
                        // 필요 시 마지막 세션 요약을 조회/계산하여 매핑
                        // summary = ...
                    }

                    return OpenCloseStatusDTO.closed(
                            last != null ? last.getStore().getId() : storeId,
                            last != null ? last.getStore().getStoreName() : null,
                            last != null ? last.getOpenedAt() : null,
                            last != null ? last.getClosedAt() : null,
                            summary
                    );
                });
    }

    @Transactional(readOnly = true)
    public CloseSummaryDTO summary() {
        UUID storeId = UUID.fromString(claimUtil.getStoreId());

        // 매장 정보
        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalStateException("매장을 찾을 수 없습니다. " + storeId));

        // 1) 현재 오픈 세션이 있으면: 지금까지 집계(마감 전 미리보기)
        var openOpt = storeOpenCloseRepository.findOpen(storeId);
        if (openOpt.isPresent()) {
            var current = openOpt.get();
            SalesSummaryDTO sum = salesRepository.summarizeByOpenClose(
                    storeId, current.getId(), SalesStatus.COMPLETED);

            long gross = (sum != null && sum.getGross() != null) ? sum.getGross() : 0L;
            long cnt   = (sum != null && sum.getCnt()   != null) ? sum.getCnt()   : 0L;

            return CloseSummaryDTO.builder()
                    .storeId(store.getId())
                    .storeName(store.getStoreName())
                    .openedAt(current.getOpenedAt())
                    .closedAt(null)            // 오픈 중이므로 null
                    .receiptCount(cnt)
                    .grossAmount(gross)
                    .build();
        }

        // 2) 오픈 세션이 없으면: 마지막(마감된) 세션 집계
        var last = storeOpenCloseRepository.findTopByStoreIdOrderByOpenedAtDesc(storeId).orElse(null);
        if (last == null) {
            return CloseSummaryDTO.builder()
                    .storeId(store.getId())
                    .storeName(store.getStoreName())
                    .openedAt(null)
                    .closedAt(null)
                    .receiptCount(0L)
                    .grossAmount(0L)
                    .build();
        }

        SalesSummaryDTO sum = salesRepository.summarizeByOpenClose(
                storeId, last.getId(), SalesStatus.COMPLETED);

        long gross = (sum != null && sum.getGross() != null) ? sum.getGross() : 0L;
        long cnt   = (sum != null && sum.getCnt()   != null) ? sum.getCnt()   : 0L;

        return CloseSummaryDTO.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .openedAt(last.getOpenedAt())
                .closedAt(last.getClosedAt())
                .receiptCount(cnt)
                .grossAmount(gross)
                .build();
    }
}
