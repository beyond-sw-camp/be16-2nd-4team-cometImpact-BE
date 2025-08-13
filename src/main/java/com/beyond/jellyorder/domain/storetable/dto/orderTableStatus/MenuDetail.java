package com.beyond.jellyorder.domain.storetable.dto.orderTableStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuDetail {
    private UUID id;
    private String name;
    private Integer quantity;
}
