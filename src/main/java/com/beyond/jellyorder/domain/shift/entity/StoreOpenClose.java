package com.beyond.jellyorder.domain.shift.entity;

import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOpenClose {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;
    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    public boolean storeOpen() { return closedAt == null; }
    public void storeClose(LocalDateTime closedAt) { this.closedAt = closedAt; }


}
