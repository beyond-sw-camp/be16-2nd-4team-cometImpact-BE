package com.beyond.jellyorder.domain.openclose.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOpenClose extends BaseIdAndTimeEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;
    @Column(name = "closed_at", nullable = true)
    private LocalDateTime closedAt;

    public boolean isOpen() {
        return closedAt == null;
    }
    public void storeClose(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }


}
