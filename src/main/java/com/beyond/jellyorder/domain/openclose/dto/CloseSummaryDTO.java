package com.beyond.jellyorder.domain.openclose.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class CloseSummaryDTO {
    private UUID storeId;
    private String storeName;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private long receiptCount;   // 영수건수
    private long grossAmount;    // 총매출
}
