package com.beyond.jellyorder.domain.store.entity;

import com.beyond.jellyorder.common.BaseIdAndTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
}
