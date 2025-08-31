// src/main/java/com/beyond/jellyorder/domain/menu/sse/MenuStatusRedisSubscriber.java
package com.beyond.jellyorder.domain.menu.sse;

import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import com.beyond.jellyorder.domain.sse.SseHub;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuStatusRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SseHub sseHub;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            MenuStatusChangedEvent evt =
                    objectMapper.readValue(message.getBody(), MenuStatusChangedEvent.class);

            String room = evt.getStoreId().toString();
            log.info("[SUB] menu-status for room={} menuId={} status={}",
                    room, evt.getMenuId(), evt.getStatus());

            // 같은 허브로 쏜다 (이게 포인트)
            sseHub.broadcast(room, "menu-status", evt);
        } catch (Exception e) {
            log.warn("[SUB] failed to handle message", e);
        }
    }
}
