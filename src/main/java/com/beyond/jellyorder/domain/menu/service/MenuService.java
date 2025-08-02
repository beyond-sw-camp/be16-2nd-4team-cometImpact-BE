package com.beyond.jellyorder.domain.menu.service;

import com.beyond.jellyorder.common.s3.S3Manager;
import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.category.repository.CategoryRepository;
import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredient;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredientId;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateReqDto;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateResDto;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.beyond.jellyorder.domain.menu.repository.MenuIngredientRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final S3Manager s3Manager;

    public MenuCreateResDto create(MenuCreateReqDto reqDto) {
        Category category = categoryRepository.findByStoreIdAndName(reqDto.getStoreId(), reqDto.getCategoryName())
                .orElseThrow(() -> new EntityNotFoundException("카테고리 조회 실패: name=" +
                        reqDto.getCategoryName() + ", storeId=" + reqDto.getStoreId()));

        MultipartFile imageFile = reqDto.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("메뉴 이미지 파일이 누락되었습니다.");
        }

        String imageUrl = null;
        Menu menu = null;

        try {
            // S3에 이미지 업로드
            imageUrl = s3Manager.upload(imageFile, "menus");

            // 메뉴 엔티티 생성 및 저장
            menu = Menu.builder()
                    .category(category)
                    .name(reqDto.getName())
                    .price(reqDto.getPrice())
                    .description(reqDto.getDescription())
                    .imageUrl(imageUrl)
                    .origin(reqDto.getOrigin())
                    .salesLimit(reqDto.getSalesLimit() != null ? reqDto.getSalesLimit() : -1L)
                    .salesToday(0)
                    .build();

            menu = menuRepository.save(menu); // UUID 생성 후 menu.id 확보

            // 식자재명 리스트에서 ID만 추출해서 MenuIngredient 구성
            List<String> ingredientNames = reqDto.getIngredients() != null ? reqDto.getIngredients() : Collections.emptyList();

            Menu finalMenu = menu;
            List<MenuIngredient> menuIngredients = ingredientNames.stream().map(ingredientName -> {
                Ingredient ingredient = ingredientRepository.findByStoreIdAndName(reqDto.getStoreId(), ingredientName)
                        .orElseThrow(() -> new EntityNotFoundException("식자재를 찾을 수 없습니다: name=" + ingredientName));

                return MenuIngredient.builder()
                        .id(new MenuIngredientId(finalMenu.getId(), ingredient.getId()))
                        // .menu(menu)          // FK 주석 처리
                        // .ingredient(ingredient) // FK 주석 처리
                        .build();
            }).collect(Collectors.toList());

            // MenuIngredientRepository를 따로 만들었다면 saveAll(menuIngredients)로 저장
            menuIngredientRepository.saveAll(menuIngredients);

        } catch (Exception e) {
            if (imageUrl != null) {
                s3Manager.delete(imageUrl);
            }
            throw e;
        }

        return MenuCreateResDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .build();
    }

    public MenuCreateResDto getMenuByStoreIdAndName(String storeId, String name) { // 추후 name 대신 UUID로 수정 예정

        // TODO [2025-08-02]: storeId 유효성 검증 (인증된 점주의 storeId인지 확인)
        // Auth 도입 후 검증 로직 추가 예정

        Menu menu = menuRepository.findByCategory_StoreIdAndName(storeId, name)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다: name=" + name + ", storeId=" + storeId));

        return MenuCreateResDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .build();
    }
}

