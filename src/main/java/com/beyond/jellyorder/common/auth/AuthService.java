package com.beyond.jellyorder.common.auth;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class AuthService {

    private final StoreRepository storeRepository;

    @Qualifier("rtInventory")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;
    private Key secret_rt_key;

    public Store validateStoreRt(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String loginId = claims.getSubject();
        Store store = storeRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("찾고자 하는 아이디가 없음"));

//        redis의 값과 비교하는 검증
        String redisRt = (String) redisTemplate.opsForValue().get(store.getLoginId());
        if (redisRt == null || !redisRt.equals(refreshToken)) { /* 현지님 의견 반영, redisRt null인 경우 추가 */
            throw new IllegalArgumentException("토큰값이 유효하지 않습니다.");
        }
        return store;
    }
}
