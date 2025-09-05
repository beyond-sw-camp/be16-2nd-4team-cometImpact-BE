package com.beyond.jellyorder.domain.websocket;

import com.beyond.jellyorder.domain.order.dto.orderStatus.OrderStatusResDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStompResDTO {
    private UUID storeId;
    private OrderStatusResDTO orderStatusResDTO;
}
