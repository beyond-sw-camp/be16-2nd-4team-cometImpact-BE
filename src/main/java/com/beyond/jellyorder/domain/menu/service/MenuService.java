package com.beyond.jellyorder.domain.menu.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.common.s3.S3Manager;
import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.category.repository.CategoryRepository;
import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.dto.IngredientDto;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredient;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateReqDto;
import com.beyond.jellyorder.domain.menu.dto.MenuCreateResDto;
import com.beyond.jellyorder.domain.menu.dto.MenuListResDto;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.option.dto.OptionAddReqDto;
import com.beyond.jellyorder.domain.option.dto.OptionAddResDto;
import com.beyond.jellyorder.domain.option.mainOption.domain.MainOption;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import com.beyond.jellyorder.domain.option.subOption.dto.SubOptionDto;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.beyond.jellyorder.domain.menu.repository.MenuIngredientRepository;

import java.util.*;
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
    private final StoreJwtClaimUtil storeJwtClaimUtil;
    private final StoreRepository storeRepository;

    public MenuCreateResDto create(MenuCreateReqDto reqDto) {
        final String storeId = storeJwtClaimUtil.getStoreId();
        storeRepository.findById(UUID.fromString(storeId)).orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        // 0) 카테고리 조회
        Category category = categoryRepository.findByStoreIdAndName(UUID.fromString(storeId), reqDto.getCategoryName())
                .orElseThrow(() -> new EntityNotFoundException(
                        "카테고리 조회 실패: name=" + reqDto.getCategoryName() + ", storeId=" + storeId));

        // 1) 옵션 트리 사전 검증 & 메모리 구성 (이미지/DB 작업 전)
        List<MainOption> preparedMainOptions = new ArrayList<>();
        Set<String> mainNames = new HashSet<>();

        List<MainOptionDto> mainOptionDtos = reqDto.getMainOptions();
        if (mainOptionDtos != null && !mainOptionDtos.isEmpty()) {
            for (int i = 0; i < mainOptionDtos.size(); i++) {
                MainOptionDto mainDto = mainOptionDtos.get(i);

                String mainName = (mainDto.getName() == null) ? "" : mainDto.getName().trim();
                if (mainName.isEmpty()) {
                    throw new IllegalArgumentException("메인 옵션 이름은 필수입니다. (index=" + i + ")");
                }
                if (!mainNames.add(mainName)) {
                    throw new IllegalArgumentException("중복된 메인 옵션 이름이 존재합니다: " + mainName);
                }

                MainOption mainOpt = MainOption.builder()
                        .name(mainName)
                        .build();

                // 서브 옵션 검증
                List<SubOption> subs = new ArrayList<>();
                Set<String> subNames = new HashSet<>();
                List<SubOptionDto> subDtos = mainDto.getSubOptions();

                if (subDtos != null && !subDtos.isEmpty()) {
                    for (int j = 0; j < subDtos.size(); j++) {
                        SubOptionDto subDto = subDtos.get(j);

                        String subName = (subDto.getName() == null) ? "" : subDto.getName().trim();
                        if (subName.isEmpty()) {
                            throw new IllegalArgumentException("서브 옵션 이름은 필수입니다. (main=" + mainName + ", index=" + j + ")");
                        }
                        if (!subNames.add(subName)) {
                            throw new IllegalArgumentException("메인 옵션 '" + mainName + "'에 중복된 서브 옵션이 존재합니다: " + subName);
                        }

                        Integer price = subDto.getPrice();
                        if (price == null) {
                            throw new IllegalArgumentException("서브 옵션 가격은 필수입니다. (main=" + mainName + ", sub=" + subName + ")");
                        }
                        if (price < 0) {
                            throw new IllegalArgumentException("서브 옵션 가격은 0 이상이어야 합니다. (main=" + mainName + ", sub=" + subName + ")");
                        }

                        subs.add(SubOption.builder()
                                .name(subName)
                                .price(price)
                                .build());
                    }
                }

                mainOpt.setSubOptions(subs);
                preparedMainOptions.add(mainOpt);
            }
        }

        // 2) 식자재 존재 검증 (DB 조회만) + 입력값 정리(트림/빈값 제거/중복 제거)
        List<String> ingredientNames = Optional.ofNullable(reqDto.getIngredients()).orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        List<Ingredient> ingredients = new ArrayList<>(ingredientNames.size());
        for (String ingName : ingredientNames) {
            Ingredient ing = ingredientRepository.findByStoreIdAndName(UUID.fromString(storeId), ingName)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "식자재를 찾을 수 없습니다: name=" + ingName + ", storeId=" + storeId));
            ingredients.add(ing);
        }

        // 3) 이미지 검증
        MultipartFile imageFile = reqDto.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("메뉴 이미지 파일이 누락되었습니다.");
        }

        String imageUrl = null;
        Menu menu;

        try {
            // 4) 이미지 업로드 (검증 모두 통과 후)
            imageUrl = s3Manager.upload(imageFile, "menus");

            // 5) 메뉴 저장 (UUID 확보)
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

            menu = menuRepository.save(menu); // UUID 확보

            // 6) 옵션 트리에 역참조 연결 후 세팅 (cascade = ALL 가정)
            for (MainOption mo : preparedMainOptions) {
                mo.setMenu(menu);
                if (mo.getSubOptions() != null) {
                    for (SubOption so : mo.getSubOptions()) {
                        so.setMainOption(mo);
                    }
                }
            }
            menu.setMainOptions(preparedMainOptions); // cascade로 함께 persist

            // 7) MenuIngredient 저장 (※ 메뉴 저장 이후)
            if (!ingredients.isEmpty()) {
                List<MenuIngredient> menuIngredients = new ArrayList<>();
                for (Ingredient ing : ingredients) {
                    MenuIngredient mi = MenuIngredient.builder()
                            .menu(menu)
                            .ingredient(ing)
                            .build();
                    menuIngredients.add(mi);
                }
                menuIngredientRepository.saveAll(menuIngredients);
            }

        } catch (Exception e) {
            // 8) 실패 시 업로드 파일 정리
            if (imageUrl != null) {
                s3Manager.delete(imageUrl);
            }
            throw e;
        }

        // 9) 응답 DTO
        return MenuCreateResDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .mainOptions(
                        menu.getMainOptions() != null ?
                                menu.getMainOptions().stream()
                                        .map(main -> MainOptionDto.builder()
                                                .name(main.getName())
                                                .subOptions(
                                                        main.getSubOptions() != null ?
                                                                main.getSubOptions().stream()
                                                                        .map(sub -> SubOptionDto.builder()
                                                                                .name(sub.getName())
                                                                                .price(sub.getPrice())
                                                                                .build())
                                                                        .toList()
                                                                : List.of()
                                                )
                                                .build())
                                        .toList()
                                : List.of()
                )
                .build();
    }

    public MenuCreateResDto getMenuByStoreIdAndName(String name) { // 추후 name 대신 UUID로 수정 예정
        final String storeId = storeJwtClaimUtil.getStoreId();
        storeRepository.findById(UUID.fromString(storeId)).orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        Menu menu = menuRepository.findByCategory_StoreIdAndName(UUID.fromString(storeId), name)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다: name=" + name + ", storeId=" + storeId));

        return MenuCreateResDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .mainOptions(
                        menu.getMainOptions() != null ?
                                menu.getMainOptions().stream()
                                        .map(main -> MainOptionDto.builder()
                                                .name(main.getName())
                                                .subOptions(
                                                        main.getSubOptions() != null ?
                                                                main.getSubOptions().stream()
                                                                        .map(sub -> SubOptionDto.builder()
                                                                                .name(sub.getName())
                                                                                .price(sub.getPrice())
                                                                                .build())
                                                                        .toList()
                                                                : List.of()
                                                )
                                                .build())
                                        .toList()
                                : List.of()
                )
                .build();
    }

    @Transactional(readOnly = true)
    public List<MenuListResDto> getMenusByStoreId() {
        final String storeId = storeJwtClaimUtil.getStoreId();
        storeRepository.findById(UUID.fromString(storeId)).orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        List<Menu> menus = menuRepository.findAllByCategory_StoreId(UUID.fromString(storeId));

        return menus.stream()
                .map(menu -> MenuListResDto.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .price(menu.getPrice())
                        .imageUrl(menu.getImageUrl())
                        .mainOptions(
                                menu.getMainOptions() != null
                                        ? menu.getMainOptions().stream()
                                        .map(main -> MainOptionDto.builder()
                                                .name(main.getName())
                                                .subOptions(
                                                        main.getSubOptions() != null
                                                                ? main.getSubOptions().stream()
                                                                .map(sub -> SubOptionDto.builder()
                                                                        .name(sub.getName())
                                                                        .price(sub.getPrice())
                                                                        .build())
                                                                .toList()
                                                                : List.of()
                                                )
                                                .build())
                                        .toList()
                                        : List.of()
                        )
                        .ingredients(
                                menu.getMenuIngredients() != null
                                        ? menu.getMenuIngredients().stream()
                                        .map(mi -> mi.getIngredient())
                                        // (선택) 같은 재료 중복 제거
                                        .collect(java.util.stream.Collectors.toMap(
                                                ing -> ing.getId(), // key
                                                ing -> IngredientDto.builder()
                                                        .id(ing.getId())
                                                        .name(ing.getName())
                                                        .build(),
                                                (a, b) -> a
                                        ))
                                        .values().stream().toList()
                                        : List.of()
                        )
                        .build())
                .toList();
    }
    
    public OptionAddResDto addOptionsToMenu(OptionAddReqDto reqDto) {
        Menu menu = menuRepository.findById(UUID.fromString(reqDto.getMenuId()))
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        // 1) 기존 메인옵션 맵과 서브옵션 이름 집합 구성
        Map<String, MainOption> mainMap = menu.getMainOptions().stream()
                .collect(Collectors.toMap(MainOption::getName, mo -> mo, (a, b) -> a));

        Map<String, Set<String>> subNameSetByMain = new HashMap<>();
        for (MainOption mo : menu.getMainOptions()) {
            Set<String> subs = mo.getSubOptions() == null ? new HashSet<>() :
                    mo.getSubOptions().stream().map(SubOption::getName).collect(Collectors.toSet());
            subNameSetByMain.put(mo.getName(), subs);
        }

        int createdMainCount = 0;
        int createdSubCount = 0;

        for (MainOptionDto mainDto : reqDto.getMainOptions()) {
            String mainName = mainDto.getName();
            MainOption targetMain = mainMap.get(mainName);

            // 요청 내에서의 서브옵션 중복도 방지
            Set<String> reqSubNamesDedup = new HashSet<>();

            // 2) 동일 메인옵션이 없으면 새로 생성
            if (targetMain == null) {
                targetMain = MainOption.builder()
                        .menu(menu)
                        .name(mainName)
                        .build();
                targetMain.setSubOptions(new ArrayList<>());
                menu.getMainOptions().add(targetMain);
                mainMap.put(mainName, targetMain);
                subNameSetByMain.put(mainName, new HashSet<>());
                createdMainCount++;
            }

            // 3) 동일 메인옵션이 있으면 그 아래에 서브옵션 추가.
            Set<String> existingSubNames = subNameSetByMain.get(mainName);

            if (mainDto.getSubOptions() != null) {
                for (SubOptionDto subDto : mainDto.getSubOptions()) {
                    String subName = subDto.getName();

                    // (검증) 이미 "메인옵션명 + 서브옵션명" 쌍이 존재하면 거부
                    if (existingSubNames.contains(subName)) {
                        throw new IllegalArgumentException(
                                "이미 존재하는 옵션입니다. main='" + mainName + "', sub='" + subName + "'"
                        );
                    }
                    // (검증) 동일 요청 본문 안에서의 서브옵션명 중복 방지
                    if (!reqSubNamesDedup.add(subName)) {
                        throw new IllegalArgumentException(
                                "요청 내 중복된 서브 옵션 이름이 있습니다. main='" + mainName + "', sub='" + subName + "'"
                        );
                    }

                    // 추가
                    SubOption sub = SubOption.builder()
                            .mainOption(targetMain)
                            .name(subName)
                            .price(subDto.getPrice())
                            .build();

                    targetMain.getSubOptions().add(sub);
                    existingSubNames.add(subName);
                    createdSubCount++;
                }
            }
        }

        return OptionAddResDto.builder()
                .menuId(menu.getId().toString())
                .addedMainOptionCount(createdMainCount) // 새로 생성된 MainOption 개수
                .addedSubOptionCount(createdSubCount)
                .build();
    }

    public void deleteMenuById(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        // S3 이미지 삭제
        if (menu.getImageUrl() != null) {
            s3Manager.delete(menu.getImageUrl());
        }

        menuRepository.delete(menu); // cascade로 관련 옵션들 자동 삭제
    }
}

