package com.beyond.jellyorder.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@MappedSuperclass
@Getter
public abstract class BaseIdEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
}