package com.beyond.jellyorder.domain.store.controller;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.domain.store.dto.PwResetCodeReqDTO;
import com.beyond.jellyorder.domain.store.dto.PwResetCodeVerifyReqDTO;
import com.beyond.jellyorder.domain.store.dto.PwResetUpdateReqDTO;
import com.beyond.jellyorder.domain.store.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /** 1) 인증코드 발송 */
    @PostMapping("/send")
    public ResponseEntity<?> sendCode(@RequestBody PwResetCodeReqDTO request) {
        passwordResetService.sendVerificationCode(request.getEmail());
        return ApiResponse.ok("인증번호 발송 완료");
    }

    /** 2) 코드 검증 → ResetToken 발급 */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody PwResetCodeVerifyReqDTO request) {
        String resetToken = passwordResetService.verifyCodeAndGetResetToken(
                request.getEmail(),
                request.getCode()
        );
        return ApiResponse.ok(resetToken, "인증번호 검증 완료");
    }

    /** 3) 비밀번호 재설정 */
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PwResetUpdateReqDTO request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.ok("비밀번호 재설정 완료");
    }
}
