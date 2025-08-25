package com.beyond.jellyorder.domain.storetable.dto.storeTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreTableLoginResDTO {
    private String storeTableAccessToken;
    private String storeTableRefreshToken;
    private UUID storeTableId;
}
