package com.beyond.jellyorder.domain.openclose.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenCloseStatusDTO {
    private boolean isOpen;           // 현재 오픈 여부
    private UUID openCloseId;         // 현재 열려있는 세션 id (isOpen=true일 때)
    private LocalDateTime openedAt;   // 현재(또는 마지막) 세션의 오픈 시각
    private LocalDateTime closedAt;   // 현재(또는 마지막) 세션의 마감 시각 (오픈 중이면 null)

    // 선택: 닫힘 상태에서 직전 마감 요약(프론트가 필요 시 표시)
    private CloseSummaryDTO summary;

    // 선택: 식별/표시용(프론트 상단 타이틀 등에 사용)
    private UUID storeId;
    private String storeName;

    /* 정적 팩토리(선택) — 서비스에서 가독성 좋게 쓰기 위한 헬퍼 */
    public static OpenCloseStatusDTO open(UUID openCloseId, UUID storeId, String storeName,
                                          LocalDateTime openedAt) {
        return OpenCloseStatusDTO.builder()
                .isOpen(true)
                .openCloseId(openCloseId)
                .storeId(storeId)
                .storeName(storeName)
                .openedAt(openedAt)
                .closedAt(null)
                .summary(null)
                .build();
    }

    public static OpenCloseStatusDTO closed(UUID storeId, String storeName,
                                            LocalDateTime openedAt, LocalDateTime closedAt,
                                            CloseSummaryDTO summary) {
        return OpenCloseStatusDTO.builder()
                .isOpen(false)
                .openCloseId(null)
                .storeId(storeId)
                .storeName(storeName)
                .openedAt(openedAt)
                .closedAt(closedAt)
                .summary(summary) // 필요 없으면 null
                .build();
    }
}
