package com.beyond.jellyorder.domain.storetable.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class StoreTableUpdateReqDTO {
    private UUID zoneId;
    private Integer seatCount;
    private String name;
}
