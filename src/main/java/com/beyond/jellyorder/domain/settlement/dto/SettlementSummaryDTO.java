package com.beyond.jellyorder.domain.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementSummaryDTO {
    private String bucket;   // 예: 2025-08-22 / 2025-W34 / 2025-08-01
    private long gross;      // 총 매출
    private long fee;        // 수수료(없으면 0)
    private long net;        // 정산 금액(없으면 gross)
    private long count;      // 건수
}
