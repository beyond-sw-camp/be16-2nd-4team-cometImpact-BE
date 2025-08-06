package com.beyond.jellyorder.domain.storetable.dto.zone;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ZoneUpdateReqDTO {
    @NotBlank(message = "zoneName은 비어 있을 수 없습니다.")
    private String zoneName;
}
