package com.beyond.jellyorder.domain.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PwResetCodeSendReqDTO {
    @NotBlank
    @Email
    private String email; // 인증코드를 받을 이메일
}
