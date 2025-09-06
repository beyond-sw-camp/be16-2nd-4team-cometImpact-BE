// src/main/java/com/beyond/jellyorder/domain/sseRequest/service/MenuStatusPublisher.java
package com.beyond.jellyorder.domain.sseRequest.service;

import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuStatusPublisher {

    @Qualifier("sseRedisTemplate")
    private final RedisTemplate<String, Object> redis;

    private final ObjectMapper objectMapper; // ⬅️ 주입 (스프링 기본 ObjectMapper)

    private String topic(java.util.UUID storeId) {
        return "tablet-menu-status:" + storeId;
    }

    public void publish(MenuStatusChangedEvent evt) {
        try {
            String t = topic(evt.getStoreId());
            String json = objectMapper.writeValueAsString(evt); // ⬅️ JSON 문자열로 변환
            log.info("[PUB] {} -> {}", t, json);
            redis.convertAndSend(t, json); // ⬅️ 문자열 발행 (StringRedisSerializer가 직렬화)
        } catch (Exception e) {
            log.warn("Failed to publish menu status", e);
        }
    }
}
