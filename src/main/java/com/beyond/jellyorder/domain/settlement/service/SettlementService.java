package com.beyond.jellyorder.domain.settlement.service;

import com.beyond.jellyorder.domain.settlement.dto.DashboardMetricsDTO;
import com.beyond.jellyorder.domain.settlement.dto.SettlementDashboardDTO;
import com.beyond.jellyorder.domain.settlement.dto.SettlementSummaryDTO;
import com.beyond.jellyorder.domain.settlement.dto.SettlementUnitDetailDTO;
import com.beyond.jellyorder.domain.settlement.repository.SettlementDetailRepository;
import com.beyond.jellyorder.domain.settlement.repository.SettlementReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {
    private final SettlementReportRepository reportRepository;
    private final SettlementDetailRepository detailRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public SettlementDashboardDTO dashboard(
            UUID storeId,
            LocalDateTime dailyFrom, LocalDateTime dailyTo,
            LocalDateTime weeklyFrom, LocalDateTime weeklyTo,
            LocalDateTime monthlyFrom, LocalDateTime monthlyTo
    ) {
        // === 오늘/주/월 기준창 계산 ===
        LocalDate today = LocalDate.now(KST);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDateTime monthStart = firstOfMonth.atStartOfDay();
        LocalDateTime nextMonthStart = firstOfMonth.plusMonths(1).atStartOfDay();

        LocalDateTime last7Start = today.minusDays(6).atStartOfDay(); // 이번주 카드용: today-6 ~ tomorrow

        // === 일, 주, 월별용 합계 ===
        long todayGross = reportRepository.sumGross(storeId, todayStart, tomorrowStart);
        long weekGross  = reportRepository.sumGross(storeId, last7Start, tomorrowStart);
        long monthGross = reportRepository.sumGross(storeId, monthStart, nextMonthStart);
        long monthNetAfterFee = Math.round(monthGross * 0.9);

        // 카드 증감률 비교 구간
        long yesterdayGross = reportRepository.sumGross(storeId,
                today.minusDays(1).atStartOfDay(),
                todayStart);

        long lastWeekGross = reportRepository.sumGross(storeId,
                last7Start.minusDays(7),
                todayStart);

        long lastMonthGross = reportRepository.sumGross(storeId,
                firstOfMonth.minusMonths(1).atStartOfDay(),
                monthStart);

        DashboardMetricsDTO metrics = DashboardMetricsDTO.fromValues(
                todayGross, yesterdayGross,
                weekGross, lastWeekGross,
                monthGross, lastMonthGross
        );

        // === 시리즈 기본 기간(미지정 시) ===
        // 일별: 최근 5일 (today-4 ~ tomorrow)
        if (dailyFrom == null || dailyTo == null) {
            dailyFrom = today.minusDays(4).atStartOfDay();
            dailyTo   = tomorrowStart;
        }
        // 주별: 최근 8주
        if (weeklyFrom == null || weeklyTo == null) {
            LocalDate mondayThisWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // 월요일
            weeklyTo   = mondayThisWeek.plusWeeks(1).atStartOfDay(); // 다음 주 시작(Exclusive)
            weeklyFrom = mondayThisWeek.minusWeeks(8).atStartOfDay(); // 8주 전 월요일
        }
        // 월별: 최근 6개월
        if (monthlyFrom == null || monthlyTo == null) {
            monthlyFrom = firstOfMonth.minusMonths(5).atStartOfDay();
            monthlyTo   = nextMonthStart;
        }

        // === 시리즈 조회 ===
        List<SettlementSummaryDTO> dailySeries = toSummaryDTOs(
                reportRepository.aggregateDaily(storeId, dailyFrom, dailyTo));

        List<SettlementSummaryDTO> weeklySeries = toWeeklyDTOs(
                reportRepository.aggregateWeekly(storeId, weeklyFrom, weeklyTo));

        List<SettlementSummaryDTO> monthlySeries = toSummaryDTOs(
                reportRepository.aggregateMonthly(storeId, monthlyFrom, monthlyTo));

        return SettlementDashboardDTO.builder()
                .metrics(metrics)
                .dailySeries(dailySeries)
                .weeklySeries(weeklySeries)
                .monthlySeries(monthlySeries)
                .dailyFrom(dailyFrom.toString()).dailyTo(dailyTo.toString())
                .weeklyFrom(weeklyFrom.toString()).weeklyTo(weeklyTo.toString())
                .monthlyFrom(monthlyFrom.toString()).monthlyTo(monthlyTo.toString())
                .build();
    }

    private static Double pct(long curr, long prev) {
        if (prev <= 0) return (curr > 0) ? 100.0 : null;
        return ((double) (curr - prev) / prev) * 100.0;
    }

    private static List<SettlementSummaryDTO> toSummaryDTOs(List<Object[]> rows) {
        return rows.stream().map(r -> {
            String bucket = String.valueOf(r[0]);
            long gross = ((Number) r[1]).longValue();
            long net   = ((Number) r[2]).longValue();
            long fee   = ((Number) r[3]).longValue();
            long cnt   = ((Number) r[4]).longValue();
            return new SettlementSummaryDTO(bucket, gross, fee, net, cnt);
        }).toList();
    }

    // 주별은 SELECT 컬럼이 bucket_key, bucket ... 이라 인덱스 주의
    private static List<SettlementSummaryDTO> toWeeklyDTOs(List<Object[]> rows) {
        return rows.stream().map(r -> {
            String bucket = String.valueOf(r[1]); // 월요일 날짜
            long gross = ((Number) r[2]).longValue();
            long net   = ((Number) r[3]).longValue();
            long fee   = ((Number) r[4]).longValue();
            long cnt   = ((Number) r[5]).longValue();
            return new SettlementSummaryDTO(bucket, gross, fee, net, cnt);
        }).toList();
    }

    @Transactional(readOnly = true)
    public Page<SettlementUnitDetailDTO> detailPage(
            UUID storeId,
            LocalDateTime from, LocalDateTime to,
            String status,                 // "COMPLETED" | "CANCELLED" | null(전체)
            Pageable pageable
    ) {
        // 기본 기간: 오늘 00:00 ~ 내일 00:00 (KST)
        ZoneId KST = ZoneId.of("Asia/Seoul");
        if (from == null || to == null) {
            var today = LocalDate.now(KST);
            from = (from == null) ? today.atStartOfDay() : from;
            to   = (to   == null) ? today.plusDays(1).atStartOfDay() : to;
        }

        Page<Object[]> headerPage = detailRepository.findUnitOrderHeaders(storeId, from, to, status, pageable);
        if (headerPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // 헤더 매핑
        Map<UUID, SettlementUnitDetailDTO> map = new LinkedHashMap<>();
        List<UUID> ids = new ArrayList<>();

        for (Object[] r : headerPage.getContent()) {
            UUID unitOrderId   = (UUID) r[0];
            String paidDate    = (String) r[1];
            String payment     = (String) r[2];
            String st          = (String) r[3];
            long totalAmount   = ((Number) r[4]).longValue();

            ids.add(unitOrderId);
            map.put(unitOrderId, SettlementUnitDetailDTO.builder()
                    .unitOrderId(unitOrderId)
                    .paidDate(paidDate)
                    .paymentMethod(payment)
                    .status(st)
                    .totalAmount(totalAmount)
                    .menus(new ArrayList<>())
                    .build());
        }

        // 메뉴/옵션 벌크 조회 후 매핑
        List<Object[]> lines = detailRepository.findMenuLinesByUnitOrders(ids);
        // unitOrderId + orderMenuId 단위로 메뉴 묶고, 옵션 축적
        class MenuKey {
            UUID unitId; UUID omId;
            MenuKey(UUID u, UUID o) { this.unitId = u; this.omId = o; }
            public boolean equals(Object o){ return o instanceof MenuKey k && k.unitId.equals(unitId) && k.omId.equals(omId); }
            public int hashCode(){ return Objects.hash(unitId, omId); }
        }
        Map<MenuKey, SettlementUnitDetailDTO.MenuLine> menuMap = new LinkedHashMap<>();

        for (Object[] r : lines) {
            UUID unitId       = (UUID) r[0];
            UUID orderMenuId  = (UUID) r[1];
            String menuName   = (String) r[2];
            Integer menuPrice = (r[3] == null) ? null : ((Number) r[3]).intValue();
            Integer qty       = (r[4] == null) ? null : ((Number) r[4]).intValue();
            String optName    = (String) r[5];
            Integer optPrice  = (r[6] == null) ? null : ((Number) r[6]).intValue();

            SettlementUnitDetailDTO dto = map.get(unitId);
            if (dto == null) continue;

            MenuKey key = new MenuKey(unitId, orderMenuId);
            SettlementUnitDetailDTO.MenuLine ml = menuMap.get(key);
            if (ml == null) {
                ml = SettlementUnitDetailDTO.MenuLine.builder()
                        .menuName(menuName)
                        .menuPrice(menuPrice)
                        .quantity(qty)
                        .options(new ArrayList<>())
                        .build();
                menuMap.put(key, ml);
                dto.getMenus().add(ml);
            }
            if (optName != null) {
                ml.getOptions().add(
                        SettlementUnitDetailDTO.OptionLine.builder()
                                .optionName(optName)
                                .optionPrice(optPrice)
                                .build()
                );
            }
        }

        List<SettlementUnitDetailDTO> content = new ArrayList<>(map.values());
        return new PageImpl<>(content, pageable, headerPage.getTotalElements());


}}
