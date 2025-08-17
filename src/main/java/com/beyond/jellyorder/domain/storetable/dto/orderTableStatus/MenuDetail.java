package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuDetail {
    private UUID menuId;
    private Integer quantity;
    private List<MenuOptionDetail> optionDetailList;
}
