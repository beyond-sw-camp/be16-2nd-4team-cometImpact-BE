// src/main/java/com/beyond/jellyorder/domain/sseRequest/service/MenuStatusEventBridge.java
package com.beyond.jellyorder.domain.sseRequest.service;

import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuStatusEventBridge {

    private final MenuStatusPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(MenuStatusChangedEvent evt) {
        log.info("[EVT] after-commit storeId={}, menuId={}, status={}",
                evt.getStoreId(), evt.getMenuId(), evt.getStatus());
        publisher.publish(evt);
    }
}
