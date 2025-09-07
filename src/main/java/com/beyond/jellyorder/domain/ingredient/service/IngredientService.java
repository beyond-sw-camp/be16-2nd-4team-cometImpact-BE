package com.beyond.jellyorder.domain.ingredient.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import com.beyond.jellyorder.domain.ingredient.dto.*;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.menu.dto.MenuStatusChangedEvent;
import com.beyond.jellyorder.domain.menu.repository.MenuIngredientRepository;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 식자재(Ingredient) 서비스
 * 정책:
 * - INSUFFICIENT(부족): 경고 상태로만 사용. 메뉴 상태 변경/이벤트 발행 없음.
 * - EXHAUSTED(소진) 진입: 연관 메뉴(수동 품절 제외)를 OUT_OF_STOCK 전환 + SSE 발행.
 * - EXHAUSTED 해제(SUFFICIENT/INSUFFICIENT로 전환): 다른 소진 재료가 없고 완판 아님, 수동 품절 아님이면 ON_SALE 복원 + SSE 발행.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final StoreJwtClaimUtil storeJwtClaimUtil;
    private final ApplicationEventPublisher eventPublisher;

    /* 생성 */
    public IngredientCreateResDto create(IngredientCreateReqDto reqDto) {
        final String storeId = storeJwtClaimUtil.getStoreId();
        Store store = storeRepository.findById(UUID.fromString(storeId))
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        if (ingredientRepository.existsByStoreIdAndName(UUID.fromString(storeId), reqDto.getName())) {
            throw new DuplicateResourceException("이미 존재하는 식자재입니다: " + reqDto.getName());
        }

        try {
            Ingredient ingredient = Ingredient.builder()
                    .store(store)
                    .name(reqDto.getName())
                    .status(reqDto.getStatus())
                    .build();

            Ingredient saved = ingredientRepository.save(ingredient);

            return IngredientCreateResDto.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .status(saved.getStatus())
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("이미 존재하는 식자재입니다: " + reqDto.getName());
        }
    }

    /* 조회 */
    @Transactional(readOnly = true)
    public IngredientListResDto getIngredientsByStoreId() {
        final String storeId = storeJwtClaimUtil.getStoreId();
        storeRepository.findById(UUID.fromString(storeId))
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        List<Ingredient> ingredients = ingredientRepository.findAllByStoreId(UUID.fromString(storeId));

        List<IngredientResDto> dtos = ingredients.stream()
                .map(i -> IngredientResDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .status(i.getStatus())
                        .build())
                .toList();

        return IngredientListResDto.builder()
                .ingredients(dtos)
                .build();
    }

    /* 삭제 */
    @Transactional
    public IngredientDeleteResDto delete(String ingredientId) {
        final String storeId = storeJwtClaimUtil.getStoreId();
        storeRepository.findById(UUID.fromString(storeId))
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        Ingredient ingredient = ingredientRepository.findById(UUID.fromString(ingredientId))
                .orElseThrow(() -> new EntityNotFoundException("식자재를 찾을 수 없습니다. id=" + ingredientId));

        if (!ingredient.getStore().getId().equals(UUID.fromString(storeId))) {
            throw new EntityNotFoundException("요청한 매장에 속하지 않는 식자재입니다. id=" + ingredientId + ", storeId=" + storeId);
        }

        // (A) 영향받는 메뉴 ID 확보
        var briefs = ingredientRepository.findAffectedMenus(ingredient.getId());
        List<UUID> affectedMenuIds = briefs.stream()
                .map(b -> UUID.fromString(b.getId()))
                .toList();

        // 실제 삭제
        ingredientRepository.delete(ingredient);

        // (B) 상태 복원 로직: 더 이상 소진 재료가 없으면 OUT_OF_STOCK → ON_SALE
        if (!affectedMenuIds.isEmpty()) {
            List<Menu> menus = menuRepository.findAllById(affectedMenuIds);
            int changed = 0;
            List<Menu> changedMenus = new java.util.ArrayList<>();

            for (Menu m : menus) {
                if (m.getStockStatus() == MenuStatus.SOLD_OUT_MANUAL) continue;

                boolean limitedSoldOut = (m.getSalesLimit() != null)
                        && !m.getSalesLimit().equals(-1)
                        && m.getSalesToday() != null
                        && m.getSalesToday().equals(m.getSalesLimit());

                if (m.getStockStatus() == MenuStatus.OUT_OF_STOCK && !limitedSoldOut) {
                    long exhaustedCnt = menuIngredientRepository.countExhaustedByMenuId(m.getId());
                    if (exhaustedCnt == 0) {
                        m.changeStockStatus(MenuStatus.ON_SALE);
                        changed++;
                        changedMenus.add(m);
                    }
                }
            }

            if (changed > 0) {
                menuRepository.saveAll(menus);
                for (Menu cm : changedMenus) {
                    publishMenuStatusIfPossible(cm);
                }
            }
        }

        var affected = briefs.stream()
                .map(b -> IngredientDeleteResDto.AffectedMenuDto.builder()
                        .id(UUID.fromString(b.getId()))
                        .name(b.getName())
                        .build())
                .toList();

        return IngredientDeleteResDto.builder()
                .ingredientId(ingredient.getId())
                .ingredientName(ingredient.getName())
                .affectedMenus(affected)
                .build();
    }

    /* 수정 */
    public IngredientModifyResDto modify(IngredientModifyReqDto req) {
        final String storeIdStr = storeJwtClaimUtil.getStoreId();
        final UUID storeId = UUID.fromString(storeIdStr);

        storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeIdStr));

        if (req.getName() == null && req.getStatus() == null) {
            throw new IllegalArgumentException("수정할 필드가 없습니다. name 또는 status 중 최소 한 개는 필요합니다.");
        }

        Ingredient ingredient = ingredientRepository
                .findByIdAndStoreId(req.getIngredientId(), storeId)
                .orElseThrow(() ->
                        new EntityNotFoundException("식자재를 찾을 수 없습니다. id=" + req.getIngredientId() + ", storeId=" + storeIdStr));

        final IngredientStatus prevStatus = ingredient.getStatus();

        // 이름 수정
        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (newName.isEmpty()) {
                throw new IllegalArgumentException("식자재명은 공백일 수 없습니다.");
            }
            boolean duplicated = ingredientRepository
                    .existsByStoreIdAndNameAndIdNot(storeId, newName, req.getIngredientId());
            if (duplicated) {
                throw new IllegalArgumentException("동일 매장 내 이미 존재하는 식자재명입니다: " + newName);
            }
            ingredient.setName(newName);
        }

        // 상태 수정
        if (req.getStatus() != null) {
            ingredient.setStatus(req.getStatus());
        }

        Ingredient saved = ingredientRepository.save(ingredient);

        /* ───────────────────────── 정책 반영 구간 ─────────────────────────
         * INSUFFICIENT은 경고만: 상태 변경/이벤트 발행 없음
         * EXHAUSTED 진입: OUT_OF_STOCK 전환 + 이벤트
         * EXHAUSTED 해제(SUFFICIENT/INSUFFICIENT): 조건 충족 시 ON_SALE 복원 + 이벤트
         * ──────────────────────────────────────────────────────────────── */

        // (1) 보유/부족 → 소진(EXHAUSTED) 전환
        if (req.getStatus() == IngredientStatus.EXHAUSTED && prevStatus != IngredientStatus.EXHAUSTED) {
            List<UUID> menuIds = menuIngredientRepository.findMenuIdsByIngredient(saved);
            if (!menuIds.isEmpty()) {
                List<Menu> menus = menuRepository.findAllById(menuIds);

                int changed = 0;
                List<Menu> changedMenus = new java.util.ArrayList<>();

                for (Menu m : menus) {
                    if (m.getStockStatus() != MenuStatus.SOLD_OUT_MANUAL
                            && m.getStockStatus() != MenuStatus.OUT_OF_STOCK) {
                        m.changeStockStatus(MenuStatus.OUT_OF_STOCK);
                        changed++;
                        changedMenus.add(m);
                    }
                }

                if (changed > 0) {
                    menuRepository.saveAll(menus);
                    for (Menu cm : changedMenus) {
                        publishMenuStatusIfPossible(cm);
                    }
                }
            }
        }

        // (2) 소진(EXHAUSTED) → 보유/부족(SUFFICIENT or INSUFFICIENT) 전환
        if (prevStatus == IngredientStatus.EXHAUSTED && req.getStatus() != IngredientStatus.EXHAUSTED) {
            List<UUID> menuIds = menuIngredientRepository.findMenuIdsByIngredient(saved);
            if (!menuIds.isEmpty()) {
                List<Menu> menus = menuRepository.findAllById(menuIds);
                List<Menu> changedMenus = new java.util.ArrayList<>();

                for (Menu m : menus) {
                    // 수동 품절은 자동 복원 금지
                    if (m.getStockStatus() == MenuStatus.SOLD_OUT_MANUAL) continue;

                    // 현재 OUT_OF_STOCK인 경우만 복원 후보
                    if (m.getStockStatus() != MenuStatus.OUT_OF_STOCK) continue;

                    // 판매 한도 완판 상태면 복원 금지
                    boolean limitedSoldOut = (m.getSalesLimit() != null)
                            && !m.getSalesLimit().equals(-1)
                            && m.getSalesToday() != null
                            && m.getSalesToday().equals(m.getSalesLimit());
                    if (limitedSoldOut) continue;

                    // 남아있는 다른 EXHAUSTED 재료가 없어야 복원
                    long exhaustedCnt = menuIngredientRepository.countExhaustedByMenuId(m.getId());
                    if (exhaustedCnt == 0) {
                        m.changeStockStatus(MenuStatus.ON_SALE);
                        changedMenus.add(m);
                    }
                }

                if (!changedMenus.isEmpty()) {
                    menuRepository.saveAll(menus);
                    for (Menu cm : changedMenus) {
                        publishMenuStatusIfPossible(cm);
                    }
                }
            }
        }

        return IngredientModifyResDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    /* SSE 이벤트 퍼블리시 헬퍼 */
    private void publishMenuStatusIfPossible(Menu m) {
        if (m == null || m.getCategory() == null || m.getCategory().getStore() == null) return;
        UUID storeId = m.getCategory().getStore().getId();
        eventPublisher.publishEvent(
                MenuStatusChangedEvent.builder()
                        .storeId(storeId)
                        .menuId(m.getId())
                        .status(m.getStockStatus())
                        .build()
        );
    }
}
