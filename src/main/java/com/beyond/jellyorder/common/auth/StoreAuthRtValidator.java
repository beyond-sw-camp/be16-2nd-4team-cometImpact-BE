package com.beyond.jellyorder.common.auth;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class StoreAuthRtValidator {

    private final StoreRepository storeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;
    private Key secret_rt_key;

    public StoreAuthRtValidator(StoreRepository storeRepository,
                                @Qualifier("storeRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.storeRepository = storeRepository;
        this.redisTemplate = redisTemplate;
    }

    /* setSigningKey에서 요구하는 secretKey 객체로 변환 != String */
    @PostConstruct
    public void init() {
        this.secret_rt_key = new SecretKeySpec(
                Base64.getDecoder().decode(secretKeyRt),
                SignatureAlgorithm.HS512.getJcaName()
        );
    }

    public Store validate(String refreshToken) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secret_rt_key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("토큰이 만료되었거나 잘못되었습니다.");
        }

        String loginId = claims.getSubject();

        Store store = storeRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Store 로그인 아이디 입니다: " + loginId));

        String redisRt = (String) redisTemplate.opsForValue().get(store.getLoginId());

        if (redisRt == null || !redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("토큰값이 유효하지 않습니다.");
        }

        return store;
    }
}
