package com.beyond.jellyorder.domain.menu.sse;

import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import com.beyond.jellyorder.domain.sseRequest.sse.SseEmitters;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuStatusToSseBridge implements MessageListener {

    private final SseEmitters emitters;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            MenuStatusChangedEvent evt =
                    objectMapper.readValue(message.getBody(), MenuStatusChangedEvent.class);

            String storeId = evt.getStoreId().toString();
            // 같은 SSE 파이프(SseEmitters)로 재송출
            emitters.send(storeId, "menu-status", evt);
        } catch (Exception e) {
            log.warn("[BRIDGE] fail forwarding menu-status to SSE", e);
        }
    }
}