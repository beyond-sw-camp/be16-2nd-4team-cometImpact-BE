package com.beyond.jellyorder.domain.storetable.dto.storeTable;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter @ToString
@NoArgsConstructor
public class StoreTableNameReqDTO {
    @NotBlank(message = "storeTableName은 비어 있을 수 없습니다.")
    private String storeTableName;
}
