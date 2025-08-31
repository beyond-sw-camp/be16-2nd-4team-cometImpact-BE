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
    private String bucket; // "yyyy-MM-dd" / "yyyy-MM-dd(주시작)" / "yyyy-MM-01"
    private long gross;
    private long fee;
    private long net;
    private long count;
}
