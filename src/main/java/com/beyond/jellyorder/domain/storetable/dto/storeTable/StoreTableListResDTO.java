package com.beyond.jellyorder.domain.storetable.dto.storeTable;

import com.beyond.jellyorder.domain.storetable.entity.Zone;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StoreTableListResDTO {
    private UUID zoneId;
    private String zoneName;
    List<StoreTableDetail> storeTableDetailList;

    public static StoreTableListResDTO from(Zone zone, List<StoreTableDetail> storeTableDetailList) {
               return StoreTableListResDTO.builder()
                       .zoneId(zone.getId())
                       .zoneName(zone.getName())
                       .storeTableDetailList(storeTableDetailList)
                       .build();
    }

}

