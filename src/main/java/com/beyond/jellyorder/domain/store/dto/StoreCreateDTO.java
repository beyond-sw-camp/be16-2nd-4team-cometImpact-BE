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

public class StoreCreateDTO {
    @NotEmpty
    private String loginId;
    @NotEmpty
    private String storeName;
    @NotEmpty
    private String businessNumber;
    @NotEmpty
    private String ownerName;
    @NotEmpty
    private String startDate;
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
                .businessNumber(this.businessNumber)
                .ownerName(this.ownerName)
                .phoneNumber(this.phoneNumber)
                .password(encodedPassword)
                .ownerEmail(this.ownerEmail)
                .build();
    }


}
