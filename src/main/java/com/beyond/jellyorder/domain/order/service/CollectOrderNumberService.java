package com.beyond.jellyorder.domain.order.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CollectOrderNumberService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "order:seq:";
    private String key(UUID storeId) {
        return KEY_PREFIX + storeId;
    }

    public CollectOrderNumberService(@Qualifier("orderNumberTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Integer collectNum(UUID storeId) {
        Long seq = redisTemplate.opsForValue().increment(key(storeId));
        if (seq == null) throw new IllegalStateException("주문번호 Redis값 증가 실패. ");
        return seq.intValue();
    }

    public long currentNum(UUID storeId) {
        String v = redisTemplate.opsForValue().get(key(storeId));
        return (v == null) ? 0L : Long.parseLong(v);
    }

    /**
     *  마감 시 해당 매장의 채번DB 초기화
     *  key값을 삭제해도 opsForValue().increment를 하면 0에서 1로 증가 및 key값 설정해 줌.
     */
    public void resetOrderNum(UUID storeId) {
        redisTemplate.delete(key(storeId));
    }

}
