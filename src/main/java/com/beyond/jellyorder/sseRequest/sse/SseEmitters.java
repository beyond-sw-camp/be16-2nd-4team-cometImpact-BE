package com.beyond.jellyorder.sseRequest.sse;

import com.beyond.jellyorder.sseRequest.dto.RequestCreateDto;
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

    // SSE 연결 추가 및 연결 해제 시 리스트에서 자동 제거
    public void add(String storeId, SseEmitter emitter) {
        // storeId로 된 emitter 리스트가 없으면 새로 생성
        emitterMap.computeIfAbsent(storeId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitterMap.remove(storeId));
        emitter.onTimeout(() -> emitterMap.remove(storeId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("연결완료"));
        } catch (IOException e) {
            emitterMap.remove(storeId);
        }
    }

    // 특정 점주에게 알림 전송
    public void notifyStore(String storeId, RequestCreateDto dto) {
        List<SseEmitter> emitters = emitterMap.get(storeId);

        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            try {
                emitter.send(SseEmitter.event().name("request").data(dto));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);  // 실패한 emitter 제거
            }
        }
    }

    // 현재 연결된 점주 수 확인용
    public void countConnectedStores() {
        System.out.println(emitterMap.size());
    }
}
