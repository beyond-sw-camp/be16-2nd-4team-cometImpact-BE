package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMenuDetailPrice {
    private String MenuName;
    private Integer MenuQuantity;
    private Integer MenuPrice;
    private List<OrderMenuOptionDetail> menuOptionList;

    public static OrderMenuDetailPrice from(OrderMenu orderMenu, List<OrderMenuOptionDetail> menuOptionList) {
        return OrderMenuDetailPrice.builder()
                .MenuName(orderMenu.getMenu().getName())
                .MenuQuantity(orderMenu.getQuantity())
                .MenuPrice(orderMenu.getMenu().getPrice())
                .menuOptionList(menuOptionList)
                .build();
    }
}
