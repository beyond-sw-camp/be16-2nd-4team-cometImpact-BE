package com.beyond.jellyorder.sseRequest.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
// 점주 -> 고객, 요청 항목 전달용
public class RequestResponseDto {
    private UUID id;
    private String name;
    private String storeId;
}
