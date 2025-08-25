package com.beyond.jellyorder.domain.sseRequest.controller;

import com.beyond.jellyorder.domain.sseRequest.sse.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 점주 SSE 구독용
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse/request")
@PreAuthorize("hasAnyRole('STORE','STORE_TABLE')")
public class RequestSseController {
    private final SseEmitters emitters;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String storeId) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(storeId, sseEmitter); // 내부에서 connect 이벤트 전송됨
        return sseEmitter;
    }
}
