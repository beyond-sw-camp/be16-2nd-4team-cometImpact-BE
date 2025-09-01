// src/main/java/com/beyond/jellyorder/domain/sse/SseHub.java
package com.beyond.jellyorder.domain.sse;

import com.beyond.jellyorder.domain.sseRequest.service.SseEmitterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class SseHub {

    private final SseEmitterRegistry registry;   // (지금은 안 쓰지만 주입은 가능)
    private final TaskScheduler taskScheduler;

    @Autowired
    public SseHub(
            SseEmitterRegistry registry,
            @Qualifier("taskScheduler") TaskScheduler taskScheduler // 충돌나는 TaskScheduler 중 이걸 명시 주입
    ) {
        this.registry = registry;
        this.taskScheduler = taskScheduler;
    }

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> rooms = new ConcurrentHashMap<>();
    private final Map<SseEmitter, ScheduledFuture<?>> heartbeats = new ConcurrentHashMap<>();

    public SseEmitter register(String room) {
        SseEmitter emitter = new SseEmitter(0L); // 무제한 타임아웃
        rooms.computeIfAbsent(room, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 연결/해제 훅
        Runnable cleanup = () -> remove(room, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> cleanup.run());

        // 연결 알림
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("ts", Instant.now().toString())));
        } catch (IOException ignore) {}

        // heartbeat (폴리필 타임아웃 방지)
        ScheduledFuture<?> hb = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data("ok"));
            } catch (Exception ex) {
                cleanup.run();
            }
        }, 20_000); // 20초마다
        heartbeats.put(emitter, hb);

        log.info("[SSE] register room={}, now={} emitters", room, rooms.get(room).size());
        return emitter;
    }

    public void broadcast(String room, String eventName, Object payload) {
        var list = rooms.get(room);
        int n = (list == null) ? 0 : list.size();
        log.info("[SSE] broadcast room={} event={} targets={}", room, eventName, n);
        if (n == 0) return;

        for (SseEmitter em : list) {
            try {
                em.send(SseEmitter.event().name(eventName).data(payload));
            } catch (Exception ex) {
                remove(room, em);
            }
        }
    }

    private void remove(String room, SseEmitter emitter) {
        var list = rooms.get(room);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) rooms.remove(room);
        }
        var hb = heartbeats.remove(emitter);
        if (hb != null) hb.cancel(false);
        log.info("[SSE] remove room={}, now={} emitters",
                room, rooms.getOrDefault(room, new CopyOnWriteArrayList<>()).size());
    }
}
