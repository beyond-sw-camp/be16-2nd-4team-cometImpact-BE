package com.beyond.jellyorder.domain.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementDashboardDTO {
    private DashboardMetricsDTO metrics;

    // 탭 전환 즉시 사용 가능하도록 세 가지 시리즈를 모두 포함
    private List<SettlementSummaryDTO> dailySeries;   // 기본: 최근 5일
    private List<SettlementSummaryDTO> weeklySeries;  // 기본: 최근 8주
    private List<SettlementSummaryDTO> monthlySeries; // 기본: 최근 6개월
    // 프런트에서는 선택된 탭에 해당하는 시리즈를 표에 그대로 바인딩
    private String dailyFrom;   // ISO 문자열 (프런트 표기용)
    private String dailyTo;
    private String weeklyFrom;
    private String weeklyTo;
    private String monthlyFrom;
    private String monthlyTo;

    public static SettlementDashboardDTO fromAggregates(
            DashboardMetricsDTO metrics,
            List<SettlementSummaryDTO> dailySeries,
            List<SettlementSummaryDTO> weeklySeries,
            List<SettlementSummaryDTO> monthlySeries,
            LocalDateTime dailyFrom, LocalDateTime dailyTo,
            LocalDateTime weeklyFrom, LocalDateTime weeklyTo,
            LocalDateTime monthlyFrom, LocalDateTime monthlyTo
    ) {
        return SettlementDashboardDTO.builder()
                .metrics(metrics)
                .dailySeries(dailySeries)
                .weeklySeries(weeklySeries)
                .monthlySeries(monthlySeries)
                .dailyFrom(dailyFrom.toString())
                .dailyTo(dailyTo.toString())
                .weeklyFrom(weeklyFrom.toString())
                .weeklyTo(weeklyTo.toString())
                .monthlyFrom(monthlyFrom.toString())
                .monthlyTo(monthlyTo.toString())
                .build();
    }

}
