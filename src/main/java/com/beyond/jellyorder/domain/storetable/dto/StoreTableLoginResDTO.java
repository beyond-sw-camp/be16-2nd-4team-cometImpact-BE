package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreTableLoginResDTO {
    private String storeTableAccessToken;
    private String storeTableRefreshToken;
}
