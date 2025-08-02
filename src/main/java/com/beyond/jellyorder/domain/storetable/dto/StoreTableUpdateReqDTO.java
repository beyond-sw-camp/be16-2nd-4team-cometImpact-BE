package com.beyond.jellyorder.domain.storetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class StoreTableUpdateReqDTO {
    private UUID zoneId;

    @NotNull(message = "seatCount는 null일 수 없습니다.")
    private Integer seatCount;

    @NotBlank(message = "name은 비어 있을 수 없습니다.")
    private String name;
}
