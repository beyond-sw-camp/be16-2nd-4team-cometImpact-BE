package com.beyond.jellyorder.domain.sse;

import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SseBroadcaster {

    private final SseRoomRepo repo;

    public void broadcastMenuStatus(MenuStatusChangedEvent evt) {
        for (SseEmitter emitter : repo.emittersOf(evt.getStoreId())) {
            try {
                emitter.send(SseEmitter.event()
                        .name("menu-status")
                        .data(evt));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}
