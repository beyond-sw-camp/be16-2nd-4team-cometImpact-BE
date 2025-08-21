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
import org.springframework.security.access.AccessDeniedException;
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

    public MenuAdminResDto update(MenuUpdateReqDto dto) {
        final UUID storeUuid = UUID.fromString(storeJwtClaimUtil.getStoreId());

        // 0) 매장 검증
        storeRepository.findById(storeUuid)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeUuid));

        // 1) 메뉴 조회 + 소속 검증
        Menu menu = menuRepository.findById(dto.getMenuId())
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        UUID menuStoreId = menu.getCategory() != null ? menu.getCategory().getStore().getId() : null; // Category에 storeId 필드 있다고 가정
        if (menuStoreId == null || !menuStoreId.equals(storeUuid)) {
            throw new AccessDeniedException("해당 메뉴는 현재 매장의 소속이 아닙니다.");
        }

        // 2) 카테고리 변경
        if (dto.getCategoryName() != null &&
                !dto.getCategoryName().equals(menu.getCategory().getName())) {
            Category newCategory = categoryRepository.findByStoreIdAndName(storeUuid, dto.getCategoryName())
                    .orElseThrow(() -> new EntityNotFoundException("카테고리 없음: " + dto.getCategoryName()));
            menu.setCategory(newCategory);
        }

        // 3) 스칼라 필드 변경 (필요 시만 set)
        if (dto.getName() != null && !Objects.equals(menu.getName(), dto.getName())) {
            menu.setName(dto.getName());
        }
        if (dto.getPrice() != null && !Objects.equals(menu.getPrice(), dto.getPrice())) {
            menu.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null && !Objects.equals(menu.getDescription(), dto.getDescription())) {
            menu.setDescription(dto.getDescription());
        }
        if (dto.getOrigin() != null && !Objects.equals(menu.getOrigin(), dto.getOrigin())) {
            menu.setOrigin(dto.getOrigin());
        }
        if (dto.getSalesLimit() != null) {
            Integer newLimit = dto.getSalesLimit();
            if (!Objects.equals(menu.getSalesLimit(), newLimit)) {
                menu.setSalesLimit(newLimit);
            }
        }
        // onSale 정책: 필요 시 수동 품절 토글로 연결
        if (dto.getOnSale() != null) {
            if (dto.getOnSale()) {
                // 수동 품절 해제
                if (menu.getStockStatus() == MenuStatus.SOLD_OUT_MANUAL) {
                    menu.markOnSale();
                }
            } else {
                // 수동 품절로 전환
                menu.markSoldOutManually();
            }
        }

        // 4) 이미지 교체 (성공 후 기존 삭제)
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String oldUrl = menu.getImageUrl();
            String newUrl = s3Manager.upload(dto.getImageFile(), "menus");
            menu.setImageUrl(newUrl);
            if (oldUrl != null) {
                s3Manager.delete(oldUrl);
            }
        }


        // 6) 옵션 트리 동기화 (이름 기반 diff)
        syncOptions(menu, dto.getMainOptions());

        // 7) 재고 상태 재계산
        if (menu.getStockStatus() != MenuStatus.SOLD_OUT_MANUAL) {
            MenuStatus computed = deriveStockStatusFromIngredients(menu);
            menu.changeStockStatus(computed);
        }

        // 8) 응답
        return MenuAdminResDto.fromEntity(menu);
    }

    /* =================== 동기화/도우미 메서드 =================== */

    private void syncIngredients(Menu menu, List<String> requestedNames, UUID storeUuid) {
        List<String> req = Optional.ofNullable(requestedNames).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        // 현재 (ingredientId -> MenuIngredient)
        Map<UUID, MenuIngredient> current = Optional.ofNullable(menu.getMenuIngredients())
                .orElseGet(List::of).stream()
                .filter(mi -> mi.getIngredient() != null)
                .collect(Collectors.toMap(mi -> mi.getIngredient().getId(), mi -> mi));

        // 요청을 엔티티로 resolve (ingredientId -> Ingredient)
        Map<UUID, Ingredient> desired = new HashMap<>();
        for (String name : req) {
            Ingredient ing = ingredientRepository.findByStoreIdAndName(storeUuid, name)
                    .orElseThrow(() -> new EntityNotFoundException("식자재 없음: " + name));
            desired.put(ing.getId(), ing);
        }

        // 제거
        if (menu.getMenuIngredients() != null) {
            for (UUID ingId : new ArrayList<>(current.keySet())) {
                if (!desired.containsKey(ingId)) {
                    menu.getMenuIngredients().remove(current.get(ingId));
                }
            }
        } else {
            menu.setMenuIngredients(new ArrayList<>());
        }

        // 추가
        for (Map.Entry<UUID, Ingredient> e : desired.entrySet()) {
            if (!current.containsKey(e.getKey())) {
                MenuIngredient mi = MenuIngredient.builder()
                        .menu(menu)
                        .ingredient(e.getValue())
                        .build();
                menu.addMenuIngredient(mi); // 편의 메서드
            }
        }
    }

    private void syncOptions(Menu menu, List<MainOptionDto> requestedMainDtos) {
        List<MainOptionDto> req = Optional.ofNullable(requestedMainDtos).orElseGet(List::of);

        // 현재 main 맵 (name -> entity)
        Map<String, MainOption> currMain = Optional.ofNullable(menu.getMainOptions())
                .orElseGet(List::of).stream()
                .collect(Collectors.toMap(MainOption::getName, mo -> mo, (a, b) -> a, LinkedHashMap::new));

        // 요청 main 맵 (name -> dto) + 검증
        Map<String, MainOptionDto> desiredMain = req.stream()
                .peek(dto -> {
                    String n = (dto.getName() == null ? "" : dto.getName().trim());
                    if (n.isEmpty()) throw new IllegalArgumentException("메인 옵션 이름은 필수입니다.");
                })
                .collect(Collectors.toMap(
                        m -> m.getName().trim(),
                        m -> m,
                        (a, b) -> { throw new IllegalArgumentException("중복 메인 옵션: " + a.getName()); },
                        LinkedHashMap::new
                ));

        // 제거될 main
        if (menu.getMainOptions() != null) {
            for (String name : new ArrayList<>(currMain.keySet())) {
                if (!desiredMain.containsKey(name)) {
                    menu.getMainOptions().remove(currMain.get(name)); // orphanRemoval
                }
            }
        } else {
            menu.setMainOptions(new ArrayList<>());
        }

        // 추가/수정 main
        for (Map.Entry<String, MainOptionDto> e : desiredMain.entrySet()) {
            String name = e.getKey();
            MainOptionDto dto = e.getValue();

            if (!currMain.containsKey(name)) {
                // 추가
                MainOption newMo = dto.toEntity(); // 내부에서 subOptions DTO→엔티티 변환/검증
                newMo.setMenu(menu);
                if (newMo.getSubOptions() != null) {
                    for (SubOption so : newMo.getSubOptions()) {
                        so.setMainOption(newMo);
                    }
                }
                menu.getMainOptions().add(newMo);
            } else {
                // 수정: 서브옵션 동기화
                MainOption mo = currMain.get(name);
                syncSubOptions(mo, dto.getSubOptions());
            }
        }
    }

    private void syncSubOptions(MainOption mo, List<SubOptionDto> requestedSubs) {
        List<SubOptionDto> req = Optional.ofNullable(requestedSubs).orElseGet(List::of);

        Map<String, SubOption> curr = Optional.ofNullable(mo.getSubOptions())
                .orElseGet(List::of).stream()
                .collect(Collectors.toMap(SubOption::getName, so -> so, (a, b) -> a, LinkedHashMap::new));

        Map<String, SubOptionDto> desired = req.stream()
                .peek(d -> {
                    String n = (d.getName() == null ? "" : d.getName().trim());
                    if (n.isEmpty()) throw new IllegalArgumentException("서브 옵션 이름은 필수입니다.");
                    if (d.getPrice() == null || d.getPrice() < 0) throw new IllegalArgumentException("서브 옵션 가격은 0 이상이어야 합니다.");
                })
                .collect(Collectors.toMap(
                        d -> d.getName().trim(),
                        d -> d,
                        (a, b) -> { throw new IllegalArgumentException("중복 서브 옵션: " + a.getName()); },
                        LinkedHashMap::new
                ));

        // 제거
        if (mo.getSubOptions() != null) {
            for (String name : new ArrayList<>(curr.keySet())) {
                if (!desired.containsKey(name)) {
                    mo.getSubOptions().remove(curr.get(name)); // orphanRemoval
                }
            }
        } else {
            mo.setSubOptions(new ArrayList<>());
        }

        // 추가/수정
        for (Map.Entry<String, SubOptionDto> e : desired.entrySet()) {
            String name = e.getKey();
            SubOptionDto dto = e.getValue();
            if (!curr.containsKey(name)) {
                SubOption so = SubOption.builder().name(name).price(dto.getPrice()).build();
                so.setMainOption(mo);
                mo.getSubOptions().add(so);
            } else {
                SubOption so = curr.get(name);
                if (!Objects.equals(so.getPrice(), dto.getPrice())) {
                    so.setPrice(dto.getPrice());
                }
            }
        }
    }
}

