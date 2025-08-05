package com.beyond.jellyorder.domain.store.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import com.beyond.jellyorder.common.auth.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Store extends BaseIdAndTimeEntity {

    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(50)")
    private String loginId;
    @Column(nullable = false)
    private String storeName;
    @Column(nullable = false, unique = true)
    private String registeredNumber;
    @Column(nullable = false)
    private String ownerName;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String ownerEmail;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.STORE;

}
