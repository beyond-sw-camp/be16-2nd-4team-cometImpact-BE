package com.beyond.jellyorder.domain.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;

    public StompController(SimpMessageSendingOperations messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    // 방법2. MessageMapping 어노테이션만 활용.
    @MessageMapping("/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageReqDTO chatMessageReqDTO
    ) {
        System.out.println("chatMessageReqDTO.message = " + chatMessageReqDTO.getMessage());
        messageTemplate.convertAndSend("/topic/" + roomId, chatMessageReqDTO);
    }







    /*
    // 방법1. MessageMapping(수신)과 Sento(topic에 메시지 전달)한꺼번에 처리
    // 어노테이션을 쓰게 된다면 pub/sub과 같은 확장성이 떨어지게 된다.
    @MessageMapping("/{roomId}") // 클라이언트에서 특정 publish/roomId 형태로 메시지를 발행 시 MessageMapping 수신
    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발행하여 구독 중인 클라이언트에게 메시지 전송
    public String sendMessage(
            // @DestinationVariable : @MessageMapping 어노테이션으로 정의된 WebSocket Controller 내에서만 사용.
            @DestinationVariable Long roomId,
            String message
    ) {
        System.out.println("message = " + message);

        return message;
    }
     */
}
