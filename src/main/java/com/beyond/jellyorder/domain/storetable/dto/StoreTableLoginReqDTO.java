package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class StoreTableLoginReqDTO {
    private Store store;
    private String name;
}
