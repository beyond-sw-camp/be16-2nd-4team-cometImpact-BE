package com.beyond.jellyorder.domain.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 스프링과 stomp는 기본적으로 세션관리를 자동(내부적)으로 처리
// 연결/해제 이벤를 기록, 연결된 세션수를 실시간으로 확인할 목적으로 이벤트 리스너를 생성 => 로그, 디버깅 목적
@Component
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void connectHandle(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        System.out.println("connect session ID = " + accessor.getSessionId());
        System.out.println("total session = " + sessions.size());
    }

    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(accessor.getSessionId());
        System.out.println("disconnect session ID = " + accessor.getSessionId());
        System.out.println("total session = " + sessions.size());
    }

    // 구독(room) 정보 확인 로직
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        System.out.println("subscribe session ID = " + sessionId);
        System.out.println("구독중인 destination(room) = " + destination);
    }

}
