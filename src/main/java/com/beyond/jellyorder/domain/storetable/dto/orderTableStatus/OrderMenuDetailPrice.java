package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMenuDetailPrice {
    private String name;
    private Integer quantity;
    private Integer price;


    public static OrderMenuDetailPrice from(OrderMenu orderMenu) {
        return OrderMenuDetailPrice.builder()
                .name(orderMenu.getMenu().getName())
                .quantity(orderMenu.getQuantity())
                .price(orderMenu.getMenu().getPrice() * orderMenu.getQuantity())
                .build();
    }
}
