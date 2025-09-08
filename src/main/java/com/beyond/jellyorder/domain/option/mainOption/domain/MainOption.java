package com.beyond.jellyorder.domain.option.mainOption.domain;


import com.beyond.jellyorder.common.BaseIdEntity;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Table(name = "main_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE main_option SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class MainOption extends BaseIdEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false, length = 20)
    private OptionSelectionType selectionType; // 필수/선택 + 단일/다중 여부

    @OneToMany(mappedBy = "mainOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubOption> subOptions;

    public Boolean getDeleted() {
        return this.deleted;
    }
}