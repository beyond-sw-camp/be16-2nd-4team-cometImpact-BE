package com.beyond.jellyorder.domain.order.dto.orderStatus;

import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateReqDTO {
    private OrderStatus orderStatus;
}
