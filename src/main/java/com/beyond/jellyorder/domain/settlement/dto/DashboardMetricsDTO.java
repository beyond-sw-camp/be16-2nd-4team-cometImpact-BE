package com.beyond.jellyorder.domain.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* 상단 카드 메트릭
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsDTO {

    private long todayGross;
    private Double vsYesterdayPct;  // null 가능

    private long weekGross;         // 오늘 기준 -7d ~ 내일0시
    private Double vsLastWeekPct;

    private long monthGross;        // 당월 1일 ~ 익월 1일
    private Double vsLastMonthPct;

    private long monthNetAfterFee;  // = Math.round(monthGross * 0.9)
}
