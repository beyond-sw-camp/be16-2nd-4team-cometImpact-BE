package com.beyond.jellyorder.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/** 주문내역 조회 - 주문내역의 한 줄(메뉴 + 수량 + 옵션들)
 * 메뉴명, 가격 변동이 되어도 주문 당시의 조건으로 DB에 저장 필요
 * 스냅샷처럼 기록
 * */
@Getter
@AllArgsConstructor
@Builder
public class TotalOrderLineResDto {
    private UUID orderMenuId;
    private UUID menuId;
    private String menuName;
    private Integer quantity;
    private Integer linePrice;  // (메뉴가격 + 옵션가격) * 수량
    private List<OrderOptionResDto> options;
}
