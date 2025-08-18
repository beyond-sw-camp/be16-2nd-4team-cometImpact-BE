package com.beyond.jellyorder.domain.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TotalOrderByTableResDto {
    private UUID totalOrderId;
    private UUID storeTableId;
    private LocalDateTime orderedAt;    // 웹소켓 연결 시 고려대상
    private Integer totalPrice;
    private Integer totalCount;
    private List<Line> lines;

    @Getter
    @Builder
    public static class Line {
        private UUID orderMenuId;
        private UUID menuId;
        private String menuName;
        private Integer menuUnitPrice;
        private Integer quantity;
        private Integer linePrice;     // (메뉴 + 옵션)의 금액 합
        private List<OptionLine> options;
    }

    @Getter
    @Builder
    public static class OptionLine {
        private String mainOptionName; // 없으면 null
        private String subOptionName;
        private Integer price;         // 옵션 금액
        private Integer quantity;
        private Integer lineTotal;
    }
}
