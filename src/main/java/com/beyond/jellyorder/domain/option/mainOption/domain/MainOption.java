package com.beyond.jellyorder.domain.option.mainOption.domain;


import com.beyond.jellyorder.common.BaseIdEntity;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "main_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainOption extends BaseIdEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false, length = 20)
    private OptionSelectionType selectionType; // 필수/선택 + 단일/다중 여부

    @OneToMany(mappedBy = "mainOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubOption> subOptions;
}