package com.beyond.jellyorder.domain.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAckDto {
    private Type type;
    private UUID storeId;
    private UUID storeTableId;
    private UUID unitOrderId;
    private String message;

    // TODO: 추후 에러처리에 대한 ERROR 타입 정의
    public enum Type {ACK, ERROR,}
}
