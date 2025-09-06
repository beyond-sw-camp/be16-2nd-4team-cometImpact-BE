package com.beyond.jellyorder.domain.sseRequest.sse;

import com.beyond.jellyorder.domain.sseRequest.dto.RequestCreateDto;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// SSE 연결 관리
@Component
public class SseEmitters {
    // 점주 식별자(email 또는 storeId)를 기준으로 Emitter 관리
    // 1 store당 다중 디바이스 연결 허용을 위한 List<SseEmitter>>
    private final Map<String, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    public void add(String storeId, SseEmitter emitter) throws IOException {
        var list = emitterMap.computeIfAbsent(storeId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        Runnable remove = () -> {
            List<SseEmitter> l = emitterMap.get(storeId);
            if (l != null) {
                l.remove(emitter);
                if (l.isEmpty()) emitterMap.remove(storeId);
            }
        };
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);

        emitter.send(SseEmitter.event().name("connected").data("연결완료"));
    }

    /** ★ 범용 전송: 어떤 이벤트 이름이든 보냄 */
    public void send(String storeId, String eventName, Object payload) {
        List<SseEmitter> emitters = emitterMap.get(storeId);
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }


    // 특정 점주에게 알림 전송
    public void notifyStore(String storeId, RequestCreateDto dto) {
        send(storeId, "request", dto);
    }

    // 현재 연결된 점주 수 확인용
    public void countConnectedStores() {
        System.out.println(emitterMap.size());
    }
}
