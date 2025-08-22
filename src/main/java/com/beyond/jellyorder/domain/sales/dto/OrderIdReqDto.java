package com.beyond.jellyorder.domain.sales.dto;

import lombok.Data;
import java.util.UUID;

/** QR 결제와 카운터 결제에서 orderId 사용하여 빌드 위한 dto */
@Data
public class OrderIdReqDto {
    private UUID orderId;
    private Long totalAmount;
    private String partner_order_id;
    private String partner_user_id;
}
