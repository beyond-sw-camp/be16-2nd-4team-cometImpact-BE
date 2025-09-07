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
import com.beyond.jellyorder.domain.option.mainOption.domain.OptionSelectionType;
import com.beyond.jellyorder.domain.option.mainOption.dto.MainOptionDto;
import com.beyond.jellyorder.domain.option.subOption.domain.SubOption;
import com.beyond.jellyorder.domain.option.subOption.dto.SubOptionDto;
import com.beyond.jellyorder.domain.order.repository.OrderMenuRepository;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private final OrderMenuRepository orderMenuRepository;

    // ★ 추가: 상태 변경 이벤트 발행용
    private final ApplicationEventPublisher eventPublisher;

    public MenuCreateResDto create(MenuCreateReqDto reqDto) {
        final UUID storeUuid = UUID.fromString(storeJwtClaimUtil.getStoreId());

        // 0) 매장 검증
        Store store = storeRepository.findById(storeUuid)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeUuid));

        // 1) 카테고리 조회/생성
        String catName = Optional.ofNullable(reqDto.getCategoryName()).map(String::trim).orElse("");
        if (catName.isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }
        Category category = categoryRepository
                .findByStoreIdAndNameAndDeletedFalse(storeUuid, reqDto.getCategoryName())
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .store(store)
                                .name(reqDto.getCategoryName())
                                .description(reqDto.getCategoryDescription())
                                .build()
                ));

        // 2) 옵션 트리 준비 (중복 이름 검증)
        List<MainOption> preparedMainOptions = new ArrayList<>();
        if (reqDto.getMainOptions() != null && !reqDto.getMainOptions().isEmpty()) {
            Set<String> dupMainNames = new HashSet<>();
            for (MainOptionDto modto : reqDto.getMainOptions()) {
                // DTO 내부에서 selectionType, subOptions 중복 검증 수행
                MainOption mo = modto.toEntity();

                String normalized = (mo.getName() == null ? "" : mo.getName().trim());
                if (normalized.isEmpty()) {
                    throw new IllegalArgumentException("메인 옵션 이름은 필수입니다.");
                }
                if (mo.getSelectionType() == null) {
                    throw new IllegalArgumentException("메인 옵션의 선택 유형은 필수입니다.");
                }
                if (!dupMainNames.add(normalized)) {
                    throw new IllegalArgumentException("중복된 메인 옵션 이름이 존재합니다: " + normalized);
                }
                mo.setName(normalized);

                // ✅ 필수 타입 정합성 (생성 전 단계에서도 1차 확인: subOptions 포함 여부)
                validateSelectionTypeConsistency(mo);

                preparedMainOptions.add(mo);
            }
        }

        // 3) 이미지 검증
        MultipartFile imageFile = reqDto.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("메뉴 이미지 파일이 누락되었습니다.");
        }

        String imageUrl = null;
        try {
            // 4) 업로드
            imageUrl = s3Manager.upload(imageFile, "menus");

            // 5) 메뉴 저장
            Menu menu = reqDto.toEntity(category, imageUrl);
            menu = menuRepository.save(menu);

            // 6) 옵션 역참조 연결(cascade 가정)
            for (MainOption mo : preparedMainOptions) {
                mo.setMenu(menu);
                if (mo.getSubOptions() != null) {
                    for (SubOption so : mo.getSubOptions()) {
                        so.setMainOption(mo);
                    }
                }
                // ✅ 연결 후 다시 한번 정합성 확인(누락 방지)
                validateSelectionTypeConsistency(mo);
            }
            menu.setMainOptions(preparedMainOptions);

            // 7) 식자재 링크 동기화 — ✅ ID 기반으로만 처리
            List<UUID> ingredientIds = Optional.ofNullable(reqDto.getIngredientIds()).orElseGet(List::of);
            syncIngredientsByIds(menu, ingredientIds, storeUuid);

            // 8) 재고 상태 재계산 (수동 품절이 아닌 경우만)
            if (menu.getStockStatus() != MenuStatus.SOLD_OUT_MANUAL) {
                MenuStatus computed = deriveStockStatusFromIngredients(menu);
                menu.changeStockStatus(computed);
            }

            // ★ 추가: 생성 직후 현재 상태를 이벤트 발행 (커밋 후 퍼블리시)
            publishMenuStatus(storeUuid, menu.getId(), menu.getStockStatus());

            // 9) 응답
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
        final UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());

        storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        List<Menu> menus = menuRepository
                .findAllByCategory_StoreIdAndDeletedFalseAndCategory_DeletedFalse(storeId);

        return menus.stream().map(MenuUserResDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuAdminResDto> getMenusForAdminByStoreId() {
        final UUID storeId = UUID.fromString(storeJwtClaimUtil.getStoreId());

        storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        List<Menu> menus = menuRepository
                .findAllByCategory_StoreIdAndDeletedFalseAndCategory_DeletedFalse(storeId);

        return menus.stream().map(MenuAdminResDto::fromEntity).toList();
    }

    public OptionAddResDto addOptionsToMenu(OptionAddReqDto reqDto) {
        Menu menu = menuRepository.findByIdAndDeletedFalse(UUID.fromString(String.valueOf(reqDto.getMenuId())))
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        // 1) 기존 메인옵션 맵과 서브옵션 이름 집합 구성 (이름 trim 기준)
        Map<String, MainOption> mainMap = menu.getMainOptions().stream()
                .collect(Collectors.toMap(mo -> mo.getName().trim(), mo -> mo, (a, b) -> a));

        Map<String, Set<String>> subNameSetByMain = new HashMap<>();
        for (MainOption mo : menu.getMainOptions()) {
            String mainKey = mo.getName().trim();
            Set<String> subs = (mo.getSubOptions() == null) ? new HashSet<>()
                    : mo.getSubOptions().stream()
                    .map(so -> so.getName() == null ? "" : so.getName().trim())
                    .collect(Collectors.toSet());
            subNameSetByMain.put(mainKey, subs);
        }

        int createdMainCount = 0;
        int createdSubCount = 0;

        for (MainOptionDto mainDto : reqDto.getMainOptions()) {
            String mainName = Optional.ofNullable(mainDto.getName()).map(String::trim).orElse("");
            if (mainName.isEmpty()) {
                throw new IllegalArgumentException("메인 옵션 이름은 필수입니다.");
            }
            if (mainDto.getSelectionType() == null) {
                throw new IllegalArgumentException("메인 옵션의 선택 유형은 필수입니다.");
            }

            MainOption targetMain = mainMap.get(mainName);
            Set<String> reqSubNamesDedup = new HashSet<>();

            if (targetMain == null) {
                // 새로 생성: selectionType 포함
                targetMain = MainOption.builder()
                        .menu(menu)
                        .name(mainName)
                        .selectionType(mainDto.getSelectionType())
                        .build();
                targetMain.setSubOptions(new ArrayList<>());
                menu.getMainOptions().add(targetMain);
                mainMap.put(mainName, targetMain);
                subNameSetByMain.put(mainName, new HashSet<>());
                createdMainCount++;
            } else {
                // 기존 메인옵션: selectionType 변경 허용
                if (targetMain.getSelectionType() != mainDto.getSelectionType()) {
                    targetMain.setSelectionType(mainDto.getSelectionType());
                }
            }

            // 서브옵션 추가
            Set<String> existingSubNames = subNameSetByMain.get(mainName);
            if (mainDto.getSubOptions() != null) {
                for (SubOptionDto subDto : mainDto.getSubOptions()) {
                    String subName = Optional.ofNullable(subDto.getName()).map(String::trim).orElse("");
                    if (subName.isEmpty()) {
                        throw new IllegalArgumentException("서브 옵션 이름은 필수입니다. main='" + mainName + "'");
                    }
                    if (subDto.getPrice() == null || subDto.getPrice() < 0) {
                        throw new IllegalArgumentException("서브 옵션 가격은 0 이상이어야 합니다. main='" + mainName + "', sub='" + subName + "'");
                    }

                    // (검증) 이미 존재하면 거부
                    if (existingSubNames.contains(subName)) {
                        throw new IllegalArgumentException(
                                "이미 존재하는 옵션입니다. main='" + mainName + "', sub='" + subName + "'"
                        );
                    }
                    // (검증) 동일 요청 본문 내 중복 방지
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

            // ✅ 필수 타입 정합성
            validateSelectionTypeConsistency(targetMain);
        }

        return OptionAddResDto.builder()
                .menuId(menu.getId().toString())
                .addedMainOptionCount(createdMainCount)
                .addedSubOptionCount(createdSubCount)
                .build();
    }

    @Transactional
    public void deleteMenuById(UUID menuId) {
        final UUID storeUuid = UUID.fromString(storeJwtClaimUtil.getStoreId());

        // 1) storeId 검증
        storeRepository.findById(storeUuid)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeUuid));

        // 2) 살아있는 메뉴만 조회 + 소속 검증
        Menu menu = menuRepository.findByIdAndDeletedFalse(menuId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));
        if (!menu.getCategory().getStore().getId().equals(storeUuid)) {
            throw new AccessDeniedException("해당 메뉴는 현재 매장의 소속이 아닙니다.");
        }

        // 3) EATING 테이블에 포함된 주문이 있으면 삭제 금지
        if (orderMenuRepository.existsInEatingTable(menuId)) {
            throw new IllegalStateException("현재 식사 중(EATING) 테이블의 주문에 포함된 메뉴는 삭제할 수 없습니다.");
        }

        // 4) (정책) 이미지: 소프트 삭제에서는 보존 권장. 필요시만 삭제.
        // if (menu.getImageUrl() != null) s3Manager.delete(menu.getImageUrl());

        // 5) 소프트 삭제 (명시적 UPDATE)
        int updated = menuRepository.softDeleteById(menuId);
        if (updated == 0) {
            throw new IllegalStateException("메뉴 소프트 삭제에 실패했습니다: " + menuId);
        }
    }

    @Transactional
    public MenuAdminResDto update(MenuUpdateReqDto dto) {
        final UUID storeUuid = UUID.fromString(storeJwtClaimUtil.getStoreId());

        // 0) 매장 검증
        Store store = storeRepository.findById(storeUuid)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeUuid));

        // 1) 메뉴 조회 + 소속 검증
        Menu menu = menuRepository.findByIdAndDeletedFalse(UUID.fromString(String.valueOf(dto.getMenuId())))
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        UUID menuStoreId = menu.getCategory() != null ? menu.getCategory().getStore().getId() : null; // Category에 storeId 필드 있다고 가정
        if (menuStoreId == null || !menuStoreId.equals(storeUuid)) {
            throw new AccessDeniedException("해당 메뉴는 현재 매장의 소속이 아닙니다.");
        }

        // ★ 추가: 변경 전 상태 백업
        MenuStatus before = menu.getStockStatus();

        // 2) 카테고리 변경
        if (StringUtils.hasText(dto.getCategoryName())) {
            String newName = dto.getCategoryName().trim();
            String currName = (menu.getCategory() != null ? menu.getCategory().getName() : null);

            if (!Objects.equals(currName, newName)) {
                Category targetCategory = categoryRepository
                        .findByStoreIdAndNameAndDeletedFalse(storeUuid, newName)
                        .orElseGet(() -> {
                            // 없으면 생성해서 바로 사용
                            Category created = Category.builder()
                                    .store(store)
                                    .name(newName)
                                    .description(dto.getCategoryDescription())
                                    .build();
                            return categoryRepository.save(created);
                        });

                menu.setCategory(targetCategory);
            }
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

        // 6.5) 옵션 정합성(전체) 재확인: 각 메인옵션에 대해 한번 더 보증
        if (menu.getMainOptions() != null) {
            for (MainOption mo : menu.getMainOptions()) {
                validateSelectionTypeConsistency(mo);
            }
        }

        // 7) 식자재 동기화
        syncIngredientsByIds(menu, dto.getIngredientIds(), storeUuid);

        // 8) 재고 상태 재계산
        if (menu.getStockStatus() != MenuStatus.SOLD_OUT_MANUAL) {
            MenuStatus computed = deriveStockStatusFromIngredients(menu);
            menu.changeStockStatus(computed);
        }

        // ★ 추가: 변경 후 상태 확인 & 달라졌을 때만 이벤트 발행
        MenuStatus after = menu.getStockStatus();
        if (before != after) {
            publishMenuStatus(storeUuid, menu.getId(), after);
        }

        // 9) 응답
        return MenuAdminResDto.fromEntity(menu);
    }

    /* =================== 동기화/도우미 메서드 =================== */

    private void syncIngredientsByIds(Menu menu, List<UUID> requestedIds, UUID storeUuid) {
        // 요청 정규화
        List<UUID> reqIds = Optional.ofNullable(requestedIds).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 현재 상태: (ingredientId -> MenuIngredient)
        Map<UUID, MenuIngredient> current = Optional.ofNullable(menu.getMenuIngredients())
                .orElseGet(List::of).stream()
                .filter(mi -> mi.getIngredient() != null)
                .collect(Collectors.toMap(mi -> mi.getIngredient().getId(), mi -> mi));

        // 요청이 빈 리스트면 모두 제거
        if (reqIds.isEmpty()) {
            if (menu.getMenuIngredients() != null) {
                // orphanRemoval=true 가정
                menu.getMenuIngredients().clear();
            } else {
                menu.setMenuIngredients(new ArrayList<>());
            }
            return;
        }

        // 요청 ID → 엔티티 resolve + 매장 소유 검증
        List<Ingredient> found = ingredientRepository.findAllById(reqIds);
        if (found.size() != reqIds.size()) {
            throw new EntityNotFoundException("존재하지 않는 식자재 ID가 포함되어 있습니다.");
        }
        boolean otherStore = found.stream().anyMatch(ing -> ing.getStore() == null
                || !Objects.equals(ing.getStore().getId(), storeUuid));
        if (otherStore) {
            throw new AccessDeniedException("다른 매장의 식자재는 연결할 수 없습니다.");
        }

        // 요청(target) 집합
        Set<UUID> desiredIds = found.stream().map(Ingredient::getId).collect(Collectors.toSet());

        // 제거: 현재 있는데 요청에 없는 링크 제거
        if (menu.getMenuIngredients() != null) {
            for (UUID curId : new ArrayList<>(current.keySet())) {
                if (!desiredIds.contains(curId)) {
                    menu.getMenuIngredients().remove(current.get(curId)); // orphanRemoval로 삭제
                }
            }
        } else {
            menu.setMenuIngredients(new ArrayList<>());
        }

        // 추가: 요청에 있는데 현재 없는 링크 추가
        for (Ingredient ing : found) {
            UUID iid = ing.getId();
            if (!current.containsKey(iid)) {
                MenuIngredient link = MenuIngredient.builder()
                        .menu(menu)
                        .ingredient(ing)
                        .build();
                menu.addMenuIngredient(link); // 편의 메서드 있으면 사용
            }
        }
    }

    private void syncOptions(Menu menu, List<MainOptionDto> requestedMainDtos) {
        List<MainOptionDto> req = Optional.ofNullable(requestedMainDtos).orElseGet(List::of);

        // 현재 main 맵 (trim 기준)
        Map<String, MainOption> currMain = Optional.ofNullable(menu.getMainOptions())
                .orElseGet(List::of).stream()
                .collect(Collectors.toMap(mo -> mo.getName().trim(), mo -> mo, (a, b) -> a, LinkedHashMap::new));

        // 요청 main 맵 (trim 기준) + 검증
        Map<String, MainOptionDto> desiredMain = req.stream()
                .peek(dto -> {
                    String n = Optional.ofNullable(dto.getName()).map(String::trim).orElse("");
                    if (n.isEmpty()) throw new IllegalArgumentException("메인 옵션 이름은 필수입니다.");
                    if (dto.getSelectionType() == null) throw new IllegalArgumentException("메인 옵션의 선택 유형은 필수입니다.");
                })
                .collect(Collectors.toMap(
                        m -> m.getName().trim(),
                        m -> m,
                        (a, b) -> { throw new IllegalArgumentException("중복 메인 옵션: " + a.getName()); },
                        LinkedHashMap::new
                ));

        // 제거
        if (menu.getMainOptions() != null) {
            for (String name : new ArrayList<>(currMain.keySet())) {
                if (!desiredMain.containsKey(name)) {
                    menu.getMainOptions().remove(currMain.get(name)); // orphanRemoval
                }
            }
        } else {
            menu.setMainOptions(new ArrayList<>());
        }

        // 추가/수정
        for (Map.Entry<String, MainOptionDto> e : desiredMain.entrySet()) {
            String name = e.getKey();
            MainOptionDto dto = e.getValue();

            if (!currMain.containsKey(name)) {
                // 추가
                MainOption newMo = dto.toEntity(); // 내부에서 subOptions, selectionType 검증/세팅
                newMo.setMenu(menu);
                if (newMo.getSubOptions() != null) {
                    for (SubOption so : newMo.getSubOptions()) {
                        so.setMainOption(newMo);
                    }
                }
                // ✅ 정합성
                validateSelectionTypeConsistency(newMo);

                menu.getMainOptions().add(newMo);
            } else {
                // 수정
                MainOption mo = currMain.get(name);

                // selectionType 변경 반영
                if (mo.getSelectionType() != dto.getSelectionType()) {
                    mo.setSelectionType(dto.getSelectionType());
                }

                // 서브옵션 동기화
                syncSubOptions(mo, dto.getSubOptions());

                // ✅ 정합성
                validateSelectionTypeConsistency(mo);
            }
        }
    }

    private void syncSubOptions(MainOption mo, List<SubOptionDto> requestedSubs) {
        List<SubOptionDto> req = Optional.ofNullable(requestedSubs).orElseGet(List::of);

        Map<String, SubOption> curr = Optional.ofNullable(mo.getSubOptions())
                .orElseGet(List::of).stream()
                .collect(Collectors.toMap(
                        so -> (so.getName() == null ? "" : so.getName().trim()),
                        so -> so, (a, b) -> a, LinkedHashMap::new
                ));

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

    private void validateSelectionTypeConsistency(MainOption mo) {
        OptionSelectionType t = mo.getSelectionType();
        int subCount = (mo.getSubOptions() == null) ? 0 : mo.getSubOptions().size();

        // 필수 타입은 후보(subOption)가 1개 이상 있어야만 의미가 있음
        if ((t == OptionSelectionType.REQUIRED_SINGLE || t == OptionSelectionType.REQUIRED_MULTIPLE)
                && subCount == 0) {
            throw new IllegalArgumentException(
                    "필수 타입의 메인 옵션('" + mo.getName() + "')은 최소 1개 이상의 서브 옵션이 필요합니다."
            );
        }
    }

    // ★ 추가: 상태 이벤트 발행 헬퍼
    private void publishMenuStatus(UUID storeId, UUID menuId, MenuStatus status) {
        eventPublisher.publishEvent(MenuStatusChangedEvent.builder()
                .storeId(storeId)
                .menuId(menuId)
                .status(status)
                .build());
    }
}
