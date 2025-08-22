package com.beyond.jellyorder.domain.sales.dto;

import com.beyond.jellyorder.domain.sales.entity.PaymentMethod;
import lombok.Data;
import java.util.UUID;

@Data
public class CounterCompleteReqDto {
    private UUID orderId;
    private PaymentMethod method;   // CARD or CASH
    private Long totalAmount;
}
