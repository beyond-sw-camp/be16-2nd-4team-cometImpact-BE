package com.beyond.jellyorder.domain.order.dto.orderStatus;

import com.beyond.jellyorder.domain.order.entity.OrderMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusMenu {
    private String menuName;
    private Integer menuQuantity;
    private List<OrderStatusMenuOption> optionList;

    public static OrderStatusMenu from(OrderMenu orderMenu) {
        return OrderStatusMenu.builder()
                .menuName(orderMenu.getMenu().getName())
                .menuQuantity(orderMenu.getQuantity())
                .optionList(orderMenu.getOrderMenuOptionList().stream()
                        .map(OrderStatusMenuOption::from).toList())
                .build();
    }
}
