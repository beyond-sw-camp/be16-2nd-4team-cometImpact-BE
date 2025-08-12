package com.beyond.jellyorder.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** 단위 주문 메뉴 DTO */
@Getter
@Setter
@NoArgsConstructor
public class UnitOrderMenuReqDto {
    private UUID menuId;
    private Integer quantity;
}
