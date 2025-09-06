package com.beyond.jellyorder.domain.order.service;

import com.beyond.jellyorder.domain.websocket.OrderStompResDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;
    private final ObjectMapper objectMapper;

    public OrderPubSubService(
            @Qualifier("orderPubSubTemplate") StringRedisTemplate stringRedisTemplate,
            SimpMessageSendingOperations messageTemplate,
            ObjectMapper objectMapper
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
        this.objectMapper = objectMapper;
    }

    // Redis에 응답 값을 발행하는 메서드
    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

    // pattern에는 topic의 이름의 패턴이 담겨있고, 이 패턴을 기반으로 다이나믹한 코딩
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Redis에서 받아온 메시지 변환
        String payload = new String(message.getBody());
        try {
            OrderStompResDTO stompResDTO = objectMapper.readValue(payload, OrderStompResDTO.class);
            // 점주 대시보드에 브로드캐스트 전달
            messageTemplate.convertAndSend("/topic/" + stompResDTO.getStoreId(), stompResDTO.getOrderStatusResDTO());
            log.info("topic에 메시지 발행 성공. destination: {}, message: {}", "/topic/" + stompResDTO.getStoreId(), stompResDTO.getOrderStatusResDTO());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
