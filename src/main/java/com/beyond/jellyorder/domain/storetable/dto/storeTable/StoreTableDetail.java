package com.beyond.jellyorder.domain.storetable.dto.storeTable;

import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.TableStatus;
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
    private TableStatus tableStatus;

    public static StoreTableDetail from(StoreTable storeTable) {
        return StoreTableDetail.builder()
                .storeTableId(storeTable.getId())
                .storeTableName(storeTable.getName())
                .seatCount(storeTable.getSeatCount())
                .tableStatus(storeTable.getStatus())
                .build();
    }
}
