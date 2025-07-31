package com.beyond.jellyorder.domain.dto;

import com.beyond.jellyorder.domain.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StoreCreateDto {
    private String loginId;
    private String storeName;
    private String registeredNumber;
    private String ownerName;
    private String phoneNumber;
    private String password;
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
