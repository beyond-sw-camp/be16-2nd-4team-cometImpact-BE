package com.beyond.jellyorder.domain.order.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOrderSequence extends BaseIdAndTimeEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @Column(name = "last_value", nullable = false)
    @Builder.Default
    private Integer lastValue = 0; // 0이면 다음은 1

    public int next() { this.lastValue = (this.lastValue==null?0:this.lastValue)+1; return this.lastValue; }
    public void reset() { this.lastValue = 0; }

}
