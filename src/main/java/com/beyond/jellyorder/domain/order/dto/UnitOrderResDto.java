package com.beyond.jellyorder.domain.order.dto;

import lombok.*;

import java.util.UUID;

/** 단위 주문 응답 DTO */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnitOrderResDto {
    private UUID totalOrderId;
    private UUID unitOrderId;
    private Integer unitPrice;  // 이번 전송 금액 합
    private Integer unitCount;  // 이번 전송 수량 합
}
