package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTableDetailResDTO {
    private UUID unitOrderId;
    private OrderStatus status;
    private Integer orderNumber;
    private LocalTime localTime;
    private List<OrderMenuDetailPrice> orderMenuList;

    public static OrderTableDetailResDTO from(UnitOrder unitOrder, List<OrderMenuDetailPrice> orderMenuList) {
        return OrderTableDetailResDTO.builder()
                .unitOrderId(unitOrder.getId())
                .status(unitOrder.getStatus())
                .orderNumber(unitOrder.getOrderNumber())
                .localTime(unitOrder.getRelevantTime())
                .orderMenuList(orderMenuList)
                .build();
    }

}
