package com.beyond.jellyorder.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 비밀번호 재설정: 새 비밀번호 변경 요청 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PwResetUpdateReqDTO {

    @NotBlank
    private String token; // /password/verify에서 발급된 ResetToken

    @NotBlank
    @Size(min = 8) // 정책에 맞게 강화 가능(대문자/특수문자 등)
    private String newPassword;
}
