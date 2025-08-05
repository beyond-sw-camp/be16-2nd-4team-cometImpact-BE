package com.beyond.jellyorder.domain.sseRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
// 고객 -> 점주, 실시간 요청용
public class RequestCreateDto implements Serializable {
    private UUID id;
    private String name;
    private String storeId;
    private String tableId;
    private String tableName;
}
