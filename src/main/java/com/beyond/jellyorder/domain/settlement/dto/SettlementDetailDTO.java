package com.beyond.jellyorder.domain.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementDetailDTO {
    private String paidDate;        // "yyyy-MM-dd HH:mm:ss"
    private String paymentMethod;   // QR/CARD/CASH
    private String menuName;        // 스냅샷
    private Integer menuPrice;
    private Integer quantity;
    private String optionName;      // nullable
    private Integer optionPrice;    // nullable
    private long lineTotal;         // (menuPrice + ΣoptionPrice) * quantity
}
