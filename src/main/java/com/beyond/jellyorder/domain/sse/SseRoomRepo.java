package com.beyond.jellyorder.domain.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SseRoomRepo {

    private final Map<UUID, CopyOnWriteArraySet<SseEmitter>> rooms = new ConcurrentHashMap<>();

    public SseEmitter add(UUID storeId, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        rooms.computeIfAbsent(storeId, k -> new CopyOnWriteArraySet<>()).add(emitter);

        Runnable clear = () -> {
            var set = rooms.get(storeId);
            if (set != null) {
                set.remove(emitter);
                if (set.isEmpty()) rooms.remove(storeId);
            }
        };
        emitter.onCompletion(clear);
        emitter.onTimeout(clear);
        emitter.onError(e -> clear.run());
        return emitter;
    }

    public Collection<SseEmitter> emittersOf(UUID storeId) {
        return rooms.getOrDefault(storeId, new CopyOnWriteArraySet<>());
    }
}
