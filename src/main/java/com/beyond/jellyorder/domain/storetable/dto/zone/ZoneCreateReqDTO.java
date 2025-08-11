package com.beyond.jellyorder.domain.storetable.dto.zone;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.storetable.entity.Zone;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ZoneCreateReqDTO {

    @NotBlank(message = "zoneName은 비어 있을 수 없습니다.")
    private String zoneName;

    public Zone toEntity(Store store) {
        return Zone.builder()
                .name(this.zoneName)
                .store(store)
                .build();
    }

}
