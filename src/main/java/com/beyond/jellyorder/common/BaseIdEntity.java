package com.beyond.jellyorder.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@MappedSuperclass
@Getter
public abstract class BaseIdEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;
}