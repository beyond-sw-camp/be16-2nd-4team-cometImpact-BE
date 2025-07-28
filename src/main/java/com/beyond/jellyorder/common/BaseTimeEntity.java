package com.beyond.jellyorder.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    @Column(updatable = false, nullable = false)
    protected LocalDateTime createdAt;

    @Column(nullable = false)
    protected LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZONE_ID);
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZONE_ID);
    }
}