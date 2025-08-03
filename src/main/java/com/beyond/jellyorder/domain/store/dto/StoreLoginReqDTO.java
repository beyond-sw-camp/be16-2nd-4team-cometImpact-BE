package com.beyond.jellyorder.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class StoreLoginReqDTO {
    private String loginId;
    private String password;
}
