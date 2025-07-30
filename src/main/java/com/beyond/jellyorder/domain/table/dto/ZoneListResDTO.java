package com.beyond.jellyorder.domain.table.dto;

import com.beyond.jellyorder.domain.table.entity.Zone;
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
