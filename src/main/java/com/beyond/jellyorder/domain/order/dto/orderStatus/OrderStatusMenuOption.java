package com.beyond.jellyorder.domain.order.dto.orderStatus;

import com.beyond.jellyorder.domain.order.entity.OrderMenuOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusMenuOption {
    private String optionName;

    public static OrderStatusMenuOption from(OrderMenuOption option) {
        return OrderStatusMenuOption.builder()
                .optionName(option.getSubOption().getName())
                .build();
    }
}
