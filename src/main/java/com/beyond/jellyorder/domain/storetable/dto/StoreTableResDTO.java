package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreTableResDTO {
    private UUID storeTableId;
    private String zoneName;
    private String name;
    private Integer seatCount;

    public static StoreTableResDTO from(StoreTable storeTable) {
        return StoreTableResDTO.builder()
                .storeTableId(storeTable.getId())
                .zoneName(storeTable.getZone().getName())
                .name(storeTable.getName())
                .seatCount(storeTable.getSeatCount())
                .build();
    }

}
