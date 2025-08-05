package com.beyond.jellyorder.domain.sseRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
// 점주 -> 고객, 요청 항목 전달용
public class RequestResponseDto {
    private UUID id;
    private String name;
    private String storeId;
    private String storeName;
}
