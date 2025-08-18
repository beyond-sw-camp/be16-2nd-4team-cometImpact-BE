package com.beyond.jellyorder.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreLoginResDTO {
    private String storeAccessToken;
    private String storeRefreshToken;
    private String storeName;
}
