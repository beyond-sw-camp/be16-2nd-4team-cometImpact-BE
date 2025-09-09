package com.beyond.jellyorder.domain.openclose.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class OpenSummaryDTO {
    private UUID openCloseId;
    private UUID storeId;
    private String storeName;
    private LocalDateTime openedAt;
}
