package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

// StoreTableListResDTO 속 테이블 dto
@Getter
@Builder
public class StoreTableDetail {
    private UUID storeTableId;
    private String storeTableName;
    private Integer seatCount;

    public static StoreTableDetail from(StoreTable storeTable) {
        return StoreTableDetail.builder()
                .storeTableId(storeTable.getId())
                .storeTableName(storeTable.getName())
                .seatCount(storeTable.getSeatCount())
                .build();
    }
}
