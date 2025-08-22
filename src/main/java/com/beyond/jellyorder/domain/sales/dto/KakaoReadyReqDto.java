package com.beyond.jellyorder.domain.sales.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class KakaoReadyReqDto {
    private UUID orderId;
    private String cid;             // 가맹점 코드
    private String partner_order_id;  // 가맹점 주문번호
    private String partner_user_id;   // 가맹점 회원 ID
    private String item_name;        // 상품명
    private Integer quantity;       // 상품 수량
    private Integer total_amount;    // 상품 총액
    private Integer tax_free_amount;  // 상품 비과세 금액
    private String approval_url;     // 결제 성공 시 redirect url
    private String cancel_url;       // 결제 취소 시 redirect url
    private String fail_url;         // 결제 실패 시 redirect url
}
