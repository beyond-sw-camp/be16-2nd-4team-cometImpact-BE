package com.beyond.jellyorder.sseRequest.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(length = 10, nullable = false)
    private String name;
}
