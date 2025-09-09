package com.beyond.jellyorder.domain.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryDTO {
    private Long gross; // 총매출 합
    private Long net;   // 정산 합 (안 쓰면 지워도 됨)
    private Long cnt;   // 건수
}
