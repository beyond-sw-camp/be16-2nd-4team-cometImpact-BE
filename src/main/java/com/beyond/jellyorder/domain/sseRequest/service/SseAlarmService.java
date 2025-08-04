package com.beyond.jellyorder.domain.sseRequest.service;

import com.beyond.jellyorder.domain.sseRequest.dto.SseMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    @Qualifier("sseRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Pub/Sub 채널에 메시지를 발행
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, @Qualifier("sseRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.redisTemplate = redisTemplate;
    }

    // 특정 사용자에게 message 발송
    public void publishMessage(String receiver, String sender, Long orderingId) {
        SseMessageDto dto = SseMessageDto.builder()
                .sender(sender)
                .receiver(receiver)
                .orderingId(orderingId)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String data = null;
        try {
            data = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // emitter 객체를 통해 메시지 전송
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);

        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name("request").data(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            redisTemplate.convertAndSend("request-channel", data);
        }
    }

    // Redis로부터 수신(Subscribe) 한 메시지를 처리
    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {

        String channel_name = new String(pattern);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());

            if (sseEmitter != null) {
                try {
                    sseEmitter.send(SseEmitter.event().name("request").data(dto));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
