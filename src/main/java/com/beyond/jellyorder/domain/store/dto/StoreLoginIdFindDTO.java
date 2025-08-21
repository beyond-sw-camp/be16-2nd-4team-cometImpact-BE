package com.beyond.jellyorder.domain.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreLoginIdFindDTO {
    @JsonProperty("ownerName")
    private String ownerName;
    @JsonProperty("businessNumber")
    private String businessNumber;
}
