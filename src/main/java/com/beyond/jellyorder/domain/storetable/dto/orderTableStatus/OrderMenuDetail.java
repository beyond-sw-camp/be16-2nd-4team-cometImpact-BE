package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMenuDetail {
    private UUID id;
    private String name;
    private Integer quantity;

    public static OrderMenuDetail from(OrderMenu orderMenu) {
        return OrderMenuDetail.builder()
                .id(orderMenu.getMenu().getId())
                .name(orderMenu.getMenu().getName())
                .quantity(orderMenu.getQuantity())
                .build();
    }
}
