package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import com.beyond.jellyorder.domain.order.entity.OrderMenuOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMenuOptionDetail {
    private String optionName;
    private Integer optionPrice;

    public static OrderMenuOptionDetail from(OrderMenuOption orderMenuOption) {
        return OrderMenuOptionDetail.builder()
                .optionName(orderMenuOption.getSubOption().getName())
                .optionPrice(orderMenuOption.getSubOption().getPrice())
                .build();
    }
}
