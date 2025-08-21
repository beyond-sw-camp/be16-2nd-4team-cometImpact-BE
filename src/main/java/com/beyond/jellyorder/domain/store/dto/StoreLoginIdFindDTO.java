package com.beyond.jellyorder.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreLoginIdFindDTO {
    private String ownerName;
    private String businessNumber;
}
