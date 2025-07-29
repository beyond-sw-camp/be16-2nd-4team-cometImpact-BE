package com.beyond.jellyorder.domain.storetable.dto;

import com.beyond.jellyorder.domain.storetable.entity.Zone;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ZoneListResDTO {
    private UUID zoneId;
    private String zoneName;

    public static ZoneListResDTO from(Zone zone) {
        return ZoneListResDTO.builder()
                .zoneId(zone.getId())
                .zoneName(zone.getName())
                .build();
    }
}
