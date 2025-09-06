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

import java.time.LocalDateTime;
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
    private String businessNumber;
    @Column(nullable = false)
    private String ownerName;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String ownerEmail;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.STORE;
    @Column(name = "business_opened_at", nullable = false)
    private LocalDateTime businessOpenedAt;
    @Column(name = "business_closed_at")
    private LocalDateTime businessClosedAt;

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeBusinessOpenedAt(LocalDateTime openedAt) {
        this.businessOpenedAt = openedAt;
    }
}
