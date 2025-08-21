package com.beyond.jellyorder.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreLoginResDTO {
    private String storeAccessToken;
    private String storeRefreshToken;
    private String storeName;
    private UUID storeId;
}
