package com.beyond.jellyorder.domain.order.dto.orderStatus;


import com.beyond.jellyorder.domain.order.entity.OrderStatus;
import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnitOrderStatusResDTO {
    private UUID unitOrderId;
    private OrderStatus status;
    private LocalTime localTime;

    public static UnitOrderStatusResDTO from(UnitOrder unitOrder) {
        return UnitOrderStatusResDTO.builder()
                .unitOrderId(unitOrder.getId())
                .status(unitOrder.getStatus())
                .localTime(unitOrder.getRelevantTime())
                .build();
    }
}
