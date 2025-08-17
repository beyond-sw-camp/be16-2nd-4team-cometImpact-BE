package com.beyond.jellyorder.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** 총 주문내역 조회
 * 메뉴명, 가격 변동이 되어도 주문 당시의 조건으로 DB에 저장 필요
 * 스냅샷처럼 기록
 * */
@Getter
@AllArgsConstructor
@Builder
public class TotalOrderDetailResDto {
    private UUID totalOrderId;
    private UUID storeTableId;
    private LocalDateTime orderedAt;
    private Integer totalPrice;    // TotalOrder.totalPrice
    private Integer totalCount;    // TotalOrder.count
    private List<TotalOrderLineResDto> lines;
}
