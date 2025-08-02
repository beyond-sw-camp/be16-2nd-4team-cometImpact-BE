package com.beyond.jellyorder.domain.menu.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateResDto {
    private UUID id;
    private String name;
    private Integer price;
    private String imageUrl;
}
