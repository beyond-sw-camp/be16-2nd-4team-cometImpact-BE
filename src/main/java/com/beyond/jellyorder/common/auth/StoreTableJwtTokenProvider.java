package com.beyond.jellyorder.common.auth;

import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.storetable.repository.StoreTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;

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
    public StoreTableJwtTokenProvider(StoreTableRepository storeTableRepository, RedisTemplate<String, Object> redisTemplate) {
        this.storeTableRepository = storeTableRepository;
        this.redisTemplate = redisTemplate;
    }


}
