package com.beyond.jellyorder.sseRequest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Request {  // extends 추가 하고 id 삭제
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(length = 10, nullable = false)
    private String name;
}
