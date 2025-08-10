package com.beyond.jellyorder.domain.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PwResetCodeVerifyReqDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "\\d{6}")
    private String code; // 6자리 숫자
}
