package com.beyond.jellyorder.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 주문내역 조회 - 주문내역의 옵셥
 * 메뉴명, 가격 변동이 되어도 주문 당시의 조건으로 DB에 저장 필요
 * 스냅샷처럼 기록
 * */
@Getter
@AllArgsConstructor
@Builder
public class OrderOptionResDto {
    private String name;
    private Integer price;
}
