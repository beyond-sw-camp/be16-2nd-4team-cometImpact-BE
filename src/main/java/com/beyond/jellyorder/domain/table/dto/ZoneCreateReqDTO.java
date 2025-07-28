package com.beyond.jellyorder.domain.table.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.table.entity.Zone;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ZoneCreateReqDTO {
    private String zoneName;

    public Zone toEntity(Store store) {
        return Zone.builder()
                .name(this.zoneName)
                .store(store)
                .build();
    }

}
