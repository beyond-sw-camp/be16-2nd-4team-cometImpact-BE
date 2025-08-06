package com.beyond.jellyorder.domain.sseRequest.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter getEmitter(String email) {
        return emitterMap.get(email);
    }

    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        emitterMap.put(email, sseEmitter);
    }

    public void removeEmitter(String email) {
        emitterMap.remove(email);
    }
}
