package com.beyond.jellyorder.domain.sseRequest.sse;

import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import com.beyond.jellyorder.domain.sse.SseHub;
import com.beyond.jellyorder.domain.sseRequest.dto.RequestCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RequestRedisSubscriber implements MessageListener {
    private final SseEmitters emitters;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            RequestCreateDto dto = om.readValue(message.getBody(), RequestCreateDto.class);
            emitters.send(dto.getStoreId(), "request", dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}