package com.beyond.jellyorder.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnitOrderResult {
    private final Integer unitPrice;
    private final Integer unitCount;
}
