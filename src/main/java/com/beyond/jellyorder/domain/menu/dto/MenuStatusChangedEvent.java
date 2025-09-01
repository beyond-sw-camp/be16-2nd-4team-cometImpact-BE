package com.beyond.jellyorder.domain.menu.dto;

import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuStatusChangedEvent {
    private UUID storeId;
    private UUID menuId;
    private MenuStatus status;
}
