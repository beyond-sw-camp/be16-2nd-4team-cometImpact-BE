package com.beyond.jellyorder.common.auth;

import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
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
 public class StoreTableJwtTokenProvider {

    private final StoreTableRepository storeTableRepository;
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
    public StoreTableJwtTokenProvider(
            StoreTableRepository storeTableRepository,
            @Qualifier("storeTableRedisTemplate") RedisTemplate<String, Object> redisTemplate
    ) {
        this.storeTableRepository = storeTableRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        secret_at_key = new SecretKeySpec(
                java.util.Base64.getDecoder().decode(secretKeyAt),
                SignatureAlgorithm.HS512.getJcaName()
        );
        secret_rt_key = new SecretKeySpec(
                java.util.Base64.getDecoder().decode(secretKeyRt),
                SignatureAlgorithm.HS512.getJcaName()
        );
    }

    // Access Token 발급
    public String createStoreTableAtToken(StoreTable table) {
        String loginId = table.getStore().getLoginId();
        String tableName = table.getName();
        String role = table.getRole().toString();

        Claims claims = Jwts.claims().setSubject(loginId);
        claims.put("role", role);
        claims.put("tableName", tableName);

        Date now = new Date();
        String storeTableAccessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationAt * 60 * 1000L))
                .signWith(secret_at_key)
                .compact();

        return storeTableAccessToken;
    }

    // Refresh Token 발급 + Redis 저장
    public String createStoreTableRtToken(StoreTable table) {
        String loginId = table.getStore().getLoginId();
        String tableName = table.getName();
        String role = table.getRole().toString();

        Claims claims = Jwts.claims().setSubject(loginId);
        claims.put("role", role);
        claims.put("tableName", tableName);

        Date now = new Date();
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
                .signWith(secret_rt_key)
                .compact();
        /* Redis에 저장: key = loginId:tableName
        * 만약 ':'을 사용하지 않고 loginId만 Redis에 저장하게 된다면 유니크함을 잃음
        * loginId: jellyorder1이고, tableName이 :로 구분이 되지 않는다면 1번 테이블, 2번 테이블 모두 같은 loginId 키값으로 구성됨
        * loginId : tableName -> jellyorder1:1번테이블, jellyorder1:2번테이블 -> 유니크함 보장
        */
        String redisKey = loginId + ":" + tableName;
        redisTemplate.opsForValue().set(redisKey, refreshToken);

        return refreshToken;
    }
}
