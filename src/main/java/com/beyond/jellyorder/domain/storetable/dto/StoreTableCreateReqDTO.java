package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import lombok.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter @ToString
@NoArgsConstructor
public class StoreTableCreateReqDTO {
    private UUID zoneId;
    private Integer seatCount;
    private List<StoreTableNameReqDTO> storeTableNameList;

    public List<StoreTable> toEntityList(Store store, Zone zone) {
        return storeTableNameList.stream()
                .map(tableNameDTO -> StoreTable.builder()
                        .store(store)
                        .zone(zone)
                        .name(tableNameDTO.getStoreTableName())
                        .seatCount(seatCount) // 기본값 외에도 DTO에서 받은 값 사용
                        .build())
                .collect(Collectors.toList());
    }
}
