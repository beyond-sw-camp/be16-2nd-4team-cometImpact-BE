package com.beyond.jellyorder.domain.sseRequest.service;

import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import com.beyond.jellyorder.domain.sse.SseBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuSseSubscriber {

    private final SseBroadcaster broadcaster;

    // MessageListenerAdapter가 직렬화해서 넘겨줌
    public void onMessage(MenuStatusChangedEvent evt) {
        try {
            broadcaster.broadcastMenuStatus(evt);
        } catch (Exception e) {
            log.warn("Failed to broadcast menu status", e);
        }
    }
}
