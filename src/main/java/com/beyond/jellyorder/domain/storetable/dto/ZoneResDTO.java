package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.storetable.entity.Zone;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ZoneResDTO {
    private UUID zoneId;
    private String zoneName;
    private UUID storeId;

    public static ZoneResDTO from(Zone zone) {
        return ZoneResDTO.builder()
                .zoneId(zone.getId())
                .zoneName(zone.getName())
                .storeId(zone.getStore().getId())
                .build();
    }

}
