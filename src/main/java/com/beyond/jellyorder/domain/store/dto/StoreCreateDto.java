package com.beyond.jellyorder.domain.store.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreCreateDto {
    @NotEmpty
    private String loginId;
    @NotEmpty
    private String storeName;
    @NotEmpty
    private String registeredNumber;
    @NotEmpty
    private String ownerName;
    @NotEmpty
    private String phoneNumber;
    @NotEmpty
    private String password;
    @NotEmpty
    private String ownerEmail;

    public Store toEntity(String encodedPassword) {
        return Store.builder()
                .loginId(this.loginId)
                .storeName(this.storeName)
                .registeredNumber(this.registeredNumber)
                .ownerName(this.ownerName)
                .phoneNumber(this.phoneNumber)
                .password(encodedPassword)
                .ownerEmail(this.ownerEmail)
                .build();
    }


}
