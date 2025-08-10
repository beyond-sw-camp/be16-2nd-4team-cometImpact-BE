package com.beyond.jellyorder.domain.store.service;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PasswordResetService {

    private final JavaMailSender mailSender;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    /** Redis DB=3 전용 템플릿(String, String) */
    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX_CODE  = "email:verify:";     // 인증코드 저장 키
    private static final String PREFIX_TOKEN = "password:reset:";    // 재설정 토큰 저장 키
    private static final long CODE_TTL_MINUTES  = 3;                 // 인증코드 유효시간(분)
    private static final long TOKEN_TTL_MINUTES = 10;                // 재설정 토큰 유효시간(분)

    public PasswordResetService(JavaMailSender mailSender, StoreRepository storeRepository, PasswordEncoder passwordEncoder, @Qualifier("passwordResetRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.mailSender = mailSender;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    /** 1) 인증코드 발송 */
    public void sendVerificationCode(String email) {
        Optional<Store> storeOpt = storeRepository.findByOwnerEmail(email);
        if (storeOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }

        String code = String.format("%06d", new Random().nextInt(1_000_000));

        // 📌 로그로 발신자/수신자/코드 확인
        log.info("[비밀번호 재설정] 발신자: {}, 수신자: {}, 인증코드: {}",
                "jellyorder.biz@gmail.com",  // yml의 spring.mail.username 값
                email,
                code
        );

        // Redis 저장(기존 코드 덮어쓰기 + TTL)
        redisTemplate.opsForValue().set(PREFIX_CODE + email, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[JellyOrder] 비밀번호 재설정 인증코드");
        message.setText("인증코드: " + code + "\n유효시간: " + CODE_TTL_MINUTES + "분");
        mailSender.send(message);
    }

    /** 2) 코드 검증 → ResetToken 발급 */
    public String verifyCodeAndGetResetToken(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(PREFIX_CODE + email);
        if (savedCode == null || !savedCode.equals(code)) {
            throw new IllegalArgumentException("인증 코드가 유효하지 않습니다.");
        }

        // 사용한 코드 제거
        redisTemplate.delete(PREFIX_CODE + email);

        // ResetToken 생성 및 저장(10분)
        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX_TOKEN + resetToken, email, TOKEN_TTL_MINUTES, TimeUnit.MINUTES);

        return resetToken;
    }

    /** 3) 비밀번호 재설정 */
    public void resetPassword(String token, String newPassword) {
        String email = redisTemplate.opsForValue().get(PREFIX_TOKEN + token);
        if (email == null) {
            throw new IllegalArgumentException("재설정 토큰이 유효하지 않습니다.");
        }

        Store store = storeRepository.findByOwnerEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        store.updatePassword(passwordEncoder.encode(newPassword));
        storeRepository.save(store);

        // 사용 완료된 토큰 삭제
        redisTemplate.delete(PREFIX_TOKEN + token);
    }
}
