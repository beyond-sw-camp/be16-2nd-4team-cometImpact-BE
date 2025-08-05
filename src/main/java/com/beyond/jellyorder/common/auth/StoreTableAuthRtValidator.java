package com.beyond.jellyorder.common.auth;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class StoreTableAuthRtValidator {

    private final StoreTableRepository storeTableRepository;
    private final StoreRepository storeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;
    private Key secret_rt_key;

    public StoreTableAuthRtValidator(StoreTableRepository storeTableRepository, StoreRepository storeRepository,
                                     @Qualifier("storeTableRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.storeTableRepository = storeTableRepository;
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

    public StoreTable validate(String refreshToken) {

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

        String storeLoginId = claims.getSubject();
        String tableName = claims.get("tableName", String.class); /* "tableName":"1번 테이블" String 형식  */

        Store store = storeRepository.findByLoginId(storeLoginId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 Store 로그인 아이디 입니다: " + storeLoginId));

        StoreTable storeTable = storeTableRepository.findByStoreAndName(store, tableName)
                .orElseThrow(() -> new EntityNotFoundException("store = " + storeLoginId +
                                                               ", tableName = " + tableName + " 에 해당하는 테이블이 존재하지 않습니다."));

        String redisKey = store.getLoginId() + ":" + tableName;
        String redisRt = (String) redisTemplate.opsForValue().get(redisKey);

        if (redisRt == null || !redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("토큰값이 유효하지 않습니다.");
        }

        return storeTable;
    }
}

