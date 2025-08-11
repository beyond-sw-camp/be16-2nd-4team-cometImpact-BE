package com.beyond.jellyorder.domain.sseRequest.entity;

import com.beyond.jellyorder.domain.request.dto.RequestUpdateDto;
import com.beyond.jellyorder.domain.store.entity.Store;
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

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store storeId;

    @Column(length = 10, nullable = false)
    private String name;

    // 테스트용
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 요청사항 수정
    public void updateRequest(RequestUpdateDto updatedto) {
        this.name = updatedto.getName();
    }
}
