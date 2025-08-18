package com.beyond.jellyorder.domain.menu.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.common.s3.S3Manager;
import com.beyond.jellyorder.domain.category.domain.Category;
import com.beyond.jellyorder.domain.category.repository.CategoryRepository;
import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuIngredient;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.menu.dto.*;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final S3Manager s3Manager;
    private final StoreJwtClaimUtil storeJwtClaimUtil;
    private final StoreRepository storeRepository;

    public MenuCreateResDto create(MenuCreateReqDto reqDto) {
        final UUID storeUuid = UUID.fromString(storeJwtClaimUtil.getStoreId());

        // 0) 매장 검증
        storeRepository.findById(storeUuid)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeUuid));

        // 1) 카테고리 조회
        Category category = categoryRepository.findByStoreIdAndName(storeUuid, reqDto.getCategoryName())
                .orElseThrow(() -> new EntityNotFoundException(
                        "카테고리 조회 실패: name=" + reqDto.getCategoryName() + ", storeId=" + storeUuid));

        // 2) 옵션 트리 생성(검증 포함)
        List<MainOption> preparedMainOptions = new ArrayList<>();
        if (reqDto.getMainOptions() != null && !reqDto.getMainOptions().isEmpty()) {
            Set<String> dupMainNames = new HashSet<>();
            for (MainOptionDto modto : reqDto.getMainOptions()) {
                MainOption mo = modto.toEntity(); // 내부에서 서브옵션 검증 포함
                if (!dupMainNames.add(mo.getName())) {
                    throw new IllegalArgumentException("중복된 메인 옵션 이름이 존재합니다: " + mo.getName());
                }
                preparedMainOptions.add(mo);
            }
        }

        // 3) 식자재 존재 검증 + 입력값 정리
        List<String> ingredientNames = Optional.ofNullable(reqDto.getIngredients()).orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        List<Ingredient> ingredients = new ArrayList<>(ingredientNames.size());
        for (String ingName : ingredientNames) {
            Ingredient ing = ingredientRepository.findByStoreIdAndName(storeUuid, ingName)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "식자재를 찾을 수 없습니다: name=" + ingName + ", storeId=" + storeUuid));
            ingredients.add(ing);
        }

        // 4) 이미지 검증
        MultipartFile imageFile = reqDto.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("메뉴 이미지 파일이 누락되었습니다.");
        }

        String imageUrl = null;
        try {
            // 5) 이미지 업로드
            imageUrl = s3Manager.upload(imageFile, "menus");

            // 6) 메뉴 저장(기본 필드 매핑)
            Menu menu = reqDto.toEntity(category, imageUrl);

            menu = menuRepository.save(menu); // UUID 확보

            // 7) 옵션 역참조 연결(cascade)
            for (MainOption mo : preparedMainOptions) {
                mo.setMenu(menu);
                if (mo.getSubOptions() != null) {
                    for (SubOption so : mo.getSubOptions()) {
                        so.setMainOption(mo);
                    }
                }
            }
            menu.setMainOptions(preparedMainOptions); // cascade로 함께 persist

            // 8) MenuIngredient 생성 & 컬렉션에만 추가
            if (!ingredients.isEmpty()) {
                Set<UUID> seen = new HashSet<>();
                for (Ingredient ing : ingredients) {
                    if (!seen.add(ing.getId())) continue; // 같은 재료 중복 방지
                    MenuIngredient mi = MenuIngredient.builder()
                            .menu(menu)
                            .ingredient(ing)
                            .build();
                    // 편의 메서드로 양방향 연결 + 컬렉션 추가
                    menu.addMenuIngredient(mi);
                }
                // menuIngredientRepository.saveAll(...) 호출하지 않습니다. (cascade로 처리)
            }

            // 9) 재고 상태 결정 (수동 품절이면 유지, 아니면 식자재 기반 산정)
            if (menu.getStockStatus() != MenuStatus.SOLD_OUT_MANUAL) {
                MenuStatus computed = deriveStockStatusFromIngredients(menu);
                menu.changeStockStatus(computed);
            }

            // 10) 응답 변환
            return MenuCreateResDto.fromEntity(menu);

        } catch (Exception e) {
            // 8) 실패 시 업로드 파일 정리
            if (imageUrl != null) {
                s3Manager.delete(imageUrl);
            }
            throw e;
        }
    }

    private MenuStatus deriveStockStatusFromIngredients(Menu menu) {
        if (menu.getMenuIngredients() == null || menu.getMenuIngredients().isEmpty()) {
            return MenuStatus.ON_SALE;
        }

        boolean anyExhausted =
                menu.getMenuIngredients().stream().anyMatch(mi -> {
                     IngredientStatus s = mi.getIngredient().getStatus();

                    return s == IngredientStatus.EXHAUSTED;
                });

        return anyExhausted ? MenuStatus.OUT_OF_STOCK : MenuStatus.ON_SALE;
    }

    @Transactional(readOnly = true)
    public List<MenuUserResDto> getMenusForUserByStoreId() {
        final String storeId = storeJwtClaimUtil.getStoreId();

        storeRepository.findById(UUID.fromString(storeId))
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        List<Menu> menus = menuRepository.findAllByCategory_StoreId(UUID.fromString(storeId));

        return menus.stream()
                .map(MenuUserResDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuAdminResDto> getMenusForAdminByStoreId() {
        final String storeId = storeJwtClaimUtil.getStoreId();

        storeRepository.findById(UUID.fromString(storeId))
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        List<Menu> menus = menuRepository.findAllByCategory_StoreId(UUID.fromString(storeId));

        return menus.stream()
                .map(MenuAdminResDto::fromEntity)
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
        final String storeId = storeJwtClaimUtil.getStoreId();
        UUID storeUuid = UUID.fromString(storeId);

        // 1) storeId 유효성 체크
        storeRepository.findById(storeUuid)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        // 2) 메뉴 조회
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        // 3) 메뉴가 해당 매장 소속인지 확인
        UUID menuStoreId = menu.getCategory().getStore().getId(); // Category 엔티티에 storeId 필드가 있다고 가정

        if (!menuStoreId.equals(storeUuid)) {
            throw new IllegalArgumentException("해당 메뉴는 현재 매장의 소속이 아닙니다.");
        }

        // 4) S3 이미지 삭제
        if (menu.getImageUrl() != null) {
            s3Manager.delete(menu.getImageUrl());
        }

        // 5) 메뉴 삭제 (옵션 등 cascade 삭제 포함)
        menuRepository.delete(menu);
    }
}

