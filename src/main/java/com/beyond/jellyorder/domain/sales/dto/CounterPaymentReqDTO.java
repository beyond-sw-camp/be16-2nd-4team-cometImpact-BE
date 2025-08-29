package com.beyond.jellyorder.domain.sales.dto;

import com.beyond.jellyorder.domain.sales.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounterPaymentReqDTO {
    private UUID totalOrderId;
    private PaymentMethod method;   // CARD or CASH
}
