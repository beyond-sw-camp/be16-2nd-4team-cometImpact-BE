package com.beyond.jellyorder.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.query.Order;

import java.util.List;
import java.util.UUID;

/** 단위 주문 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class UnitOrderCreateReqDto {
    private UUID storeTableId;
    List<UnitOrderMenuReqDto> menus;
}
