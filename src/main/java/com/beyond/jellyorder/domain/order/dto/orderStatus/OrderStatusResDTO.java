package com.beyond.jellyorder.domain.order.dto.orderStatus;

import com.beyond.jellyorder.domain.order.entity.UnitOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusResDTO {
    private UUID unitOrderId;
    private Integer orderNumber;
    private String storeTableName;
    private LocalTime localTime;
    private List<OrderStatusMenu> orderMenuList;

    public static OrderStatusResDTO from(UnitOrder unitOrder) {
        return OrderStatusResDTO.builder()
                .unitOrderId(unitOrder.getId())
                .orderNumber(unitOrder.getOrderNumber())
                .storeTableName(unitOrder.getTotalOrder().getStoreTable().getName()) // join이 너무 많음.
                .localTime(unitOrder.getRelevantTime())
                .orderMenuList(unitOrder.getOrderMenus().stream().map(OrderStatusMenu::from).toList())
                .build();
    }

    public static OrderStatusResDTO from(UnitOrder unitOrder, String storeTableName) {
        return OrderStatusResDTO.builder()
                .unitOrderId(unitOrder.getId())
                .orderNumber(unitOrder.getOrderNumber())
                .storeTableName(storeTableName)
                .localTime(unitOrder.getRelevantTime())
                .orderMenuList(unitOrder.getOrderMenus().stream().map(OrderStatusMenu::from).toList())
                .build();
    }

}
