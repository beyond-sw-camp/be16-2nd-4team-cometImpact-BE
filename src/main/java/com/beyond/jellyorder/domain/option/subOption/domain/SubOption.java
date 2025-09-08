package com.beyond.jellyorder.domain.option.subOption.domain;

import com.beyond.jellyorder.common.BaseIdEntity;
import com.beyond.jellyorder.domain.option.mainOption.domain.MainOption;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "sub_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE sub_option SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class SubOption extends BaseIdEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_option_id", nullable = false)
    private MainOption mainOption;

    @NotBlank(message = "서브 옵션 이름은 필수입니다.")
    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean deleted = false;

    @NotNull(message = "서브 옵션 가격은 필수입니다.")
    @Min(value = 0, message = "서브 옵션 가격은 0 이상이어야 합니다.")
    @Column(name = "price", nullable = false)
    private Integer price;

    public Boolean getDeleted() {
        return this.deleted;
    }
}