package com.beyond.jellyorder.common.auth;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component

public class StoreJwtTokenProvider {
    private final StoreRepository storeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.expirationAt}")
    private int expirationAt;
    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;
    private Key secret_rt_key;

    @Autowired
    public StoreJwtTokenProvider(StoreRepository storeRepository, @Qualifier("storeAuthRedisConnectionFactory") RedisTemplate<String, Object> redisTemplate) {
        this.storeRepository = storeRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        secret_at_key = new SecretKeySpec
                (java.util.Base64.getDecoder().decode(secretKeyAt), // 인코딩 된 걸 디코딩
                SignatureAlgorithm.HS512.getJcaName()); // decode + algorithm 세팅
        secret_rt_key = new SecretKeySpec
                (java.util.Base64.getDecoder().decode(secretKeyRt),
                        SignatureAlgorithm.HS512.getJcaName());
    }

    public String createStoreAtToken(Store store) {
        String loginId = store.getLoginId();
        String role = store.getRole().toString();

        Claims claims = Jwts.claims().setSubject(loginId); // filter에서 claims.getSubject와 싱크, loginId
        claims.put("role", role);
        Date now = new Date();
        String storeAccessToken = Jwts.builder() // 토큰 제작
                .setClaims(claims) // loginId + role STORE
                .setIssuedAt(now) // 발행시간
                .setExpiration(new Date(now.getTime() + expirationAt*60*1000L)) // 만료시간, 1000분
                .signWith(secret_at_key)
                .compact();

        return storeAccessToken;
}

    public String createStoreRtToken(Store store) {
//        유효기간이 긴 rt 토큰 생성
        String loginId = store.getLoginId();
        String role = store.getRole().toString();

        Claims claims = Jwts.claims().setSubject(loginId); // filter에서 claims.getSubject와 싱크, loginId
        claims.put("role", role);
        Date now = new Date();
        String storeRefreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 발행시간
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L)) // 만료시간, 10000분 설정
                .signWith(secret_rt_key)
                .compact();

        redisTemplate.opsForValue().set(store.getLoginId(), storeRefreshToken);

        return storeRefreshToken;
    }





}
