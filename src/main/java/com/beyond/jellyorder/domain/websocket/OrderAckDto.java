package com.beyond.jellyorder.domain.websocket;

import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAckDto {
    private String reqId;            // 요청과 매칭
    private boolean ok;              // true=성공, false=실패
    private String code;             // "OK", "OUT_OF_STOCK", "QUANTITY_MISMATCH", ...
    private String message;          // 사용자 노출 메시지
    private OrderStatusResDTO data;  // 성공 시 주문 데이터(선택)
}
