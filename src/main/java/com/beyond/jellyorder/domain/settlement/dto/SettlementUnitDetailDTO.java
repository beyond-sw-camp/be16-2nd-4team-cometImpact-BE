package com.beyond.jellyorder.domain.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementUnitDetailDTO {
    private UUID receiptId;
    private String receiptNo; // 주문번호, 추후 도입
    private String paidDate;          // "yyyy-MM-dd HH:mm:ss" (CANCELLED이면 updated_at 기준)
    private String paymentMethod;     // CARD/CASH/QR (nullable 가능)
    private String status;            // COMPLETED/CANCELLED
    private long totalAmount;         // (menuPrice + ΣoptionPrice) * quantity 의 합(주문 전체)
    private List<MenuLine> menus;     // 메뉴 + 옵션 목록

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MenuLine {
        private String menuName;
        private Integer menuPrice;
        private Integer quantity;
        private List<OptionLine> options; // 옵션 여러 개
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionLine {
        private String optionName;
        private Integer optionPrice;
    }


}
