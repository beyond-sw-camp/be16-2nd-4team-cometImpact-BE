package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTableUpdateReqDTO {
    private UUID unitOrderId;
    private Boolean isDeleteAll;
    private List<MenuDetail> addMenuList;
    private List<MenuDetail> deleteMenuList;
}
