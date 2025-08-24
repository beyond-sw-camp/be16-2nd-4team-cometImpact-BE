package com.beyond.jellyorder.domain.ingredient.service;

import com.beyond.jellyorder.common.auth.StoreJwtClaimUtil;
import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.domain.IngredientStatus;
import com.beyond.jellyorder.domain.ingredient.dto.*;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import com.beyond.jellyorder.domain.menu.domain.Menu;
import com.beyond.jellyorder.domain.menu.domain.MenuStatus;
import com.beyond.jellyorder.domain.menu.repository.MenuIngredientRepository;
import com.beyond.jellyorder.domain.menu.repository.MenuRepository;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 식자재(Ingredient) 관련 비즈니스 로직을 담당하는 서비스 클래스.
 * - 식자재 생성
 * - 중복 이름 검사
 * - 매장별 식자재 관리 등
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

    /**
     * 새로운 식자재를 생성한다.
     * - 동일 매장(storeId) 내에서 이름(name)이 중복되면 예외를 발생시킨다.
     * - 생성된 식자재를 저장 후 응답 DTO로 변환하여 반환한다.
     *
     * @param reqDto 클라이언트로부터 전달받은 식자재 생성 요청 DTO
     * @return 생성된 식자재 정보를 담은 응답 DTO
     * @throws DuplicateResourceException 동일 매장 내에 이름이 중복되는 경우
     */
    public IngredientCreateResDto create(IngredientCreateReqDto reqDto) {
        final String storeId = storeJwtClaimUtil.getStoreId();
        Store store = storeRepository.findById(UUID.fromString(storeId)).orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        if (ingredientRepository.existsByStoreIdAndName(UUID.fromString(storeId), reqDto.getName())) {
            throw new DuplicateResourceException("이미 존재하는 식자재입니다: " + reqDto.getName());
        }

        try {
            // 식자재 엔티티 생성
            Ingredient ingredient = Ingredient.builder()
                    .store(store)
                    .name(reqDto.getName())
                    .status(reqDto.getStatus())
                    .build();

            // DB에 저장
            Ingredient saved = ingredientRepository.save(ingredient);

            // 응답 DTO 반환
            return IngredientCreateResDto.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .status(saved.getStatus())
                    .build();

        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 인한 DB 중복 제약 위반 시 예외 변환
            throw new DuplicateResourceException("이미 존재하는 식자재입니다: " + reqDto.getName());
        }
    }

    @Transactional(readOnly = true)
    public IngredientListResDto getIngredientsByStoreId() {
        final String storeId = storeJwtClaimUtil.getStoreId();
        storeRepository.findById(UUID.fromString(storeId)).orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        // 2) 원재료 조회
        List<Ingredient> ingredients = ingredientRepository.findAllByStoreId(UUID.fromString(storeId));

        // 3) DTO 변환 (재료가 없으면 빈 리스트)
        List<IngredientResDto> dtos = ingredients.stream()
                .map(i -> IngredientResDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .status(i.getStatus())
                        .build())
                .toList();

        // 4) 결과 반환
        return IngredientListResDto.builder()
                .ingredients(dtos)
                .build();
    }

    @Transactional
    public IngredientDeleteResDto delete(String ingredientId) {
        final String storeId = storeJwtClaimUtil.getStoreId();
        storeRepository.findById(UUID.fromString(storeId))
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 storeId: " + storeId));

        // 1) 대상 조회
        Ingredient ingredient = ingredientRepository.findById(UUID.fromString(ingredientId))
                .orElseThrow(() -> new EntityNotFoundException("식자재를 찾을 수 없습니다. id=" + ingredientId));

        // 2) 소속(storeId) 검증
        if (!ingredient.getStore().getId().equals(UUID.fromString(storeId))) {
            throw new EntityNotFoundException("요청한 매장에 속하지 않는 식자재입니다. id=" + ingredientId + ", storeId=" + storeId);
        }

        // ✅ (A) 영향받는 메뉴 ID들을 미리 확보
        var briefs = ingredientRepository.findAffectedMenus(ingredient.getId());
        List<UUID> affectedMenuIds = briefs.stream()
                .map(b -> UUID.fromString(b.getId()))
                .toList();

        // 3) 삭제
        ingredientRepository.delete(ingredient);

        // ✅ (B) 상태 복원 로직
        if (!affectedMenuIds.isEmpty()) {
            List<Menu> menus = menuRepository.findAllById(affectedMenuIds);
            int changed = 0;

            for (Menu m : menus) {
                // 수동 품절은 건드리지 않음
                if (m.getStockStatus() == MenuStatus.SOLD_OUT_MANUAL) continue;

                // 현재 OUT_OF_STOCK 이고, 판매제한으로 인한 소진 상태가 아니어야 함
                boolean limitedSoldOut = (m.getSalesLimit() != null)
                        && !m.getSalesLimit().equals(-1)
                        && m.getSalesToday() != null
                        && m.getSalesToday().equals(m.getSalesLimit());

                if (m.getStockStatus() == MenuStatus.OUT_OF_STOCK && !limitedSoldOut) {
                    // 남아있는 연관 식자재 중 EXHAUSTED 가 하나라도 있으면 그대로 유지
                    long exhaustedCnt = menuIngredientRepository.countExhaustedByMenuId(m.getId());
                    if (exhaustedCnt == 0) {
                        m.changeStockStatus(MenuStatus.ON_SALE);
                        changed++;
                    }
                }
            }

            if (changed > 0) {
                menuRepository.saveAll(menus);
            }
        }

        // 4) 응답 조립
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

        // 기존 상태 백업
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

        // ✅ EXHAUSTED 로 변경된 경우: 이 재료를 쓰는 메뉴(동일 매장, 수동품절 제외)를 OUT_OF_STOCK 로 전환
        if (req.getStatus() == IngredientStatus.EXHAUSTED && prevStatus != IngredientStatus.EXHAUSTED) {
            // 1) 조인테이블에서 메뉴 ID만 조회
            List<UUID> menuIds = menuIngredientRepository.findMenuIdsByIngredient(saved);
            if (!menuIds.isEmpty()) {
                // 2) 해당 메뉴들 로드
                List<Menu> menus = menuRepository.findAllById(menuIds);

                int changed = 0;
                for (Menu m : menus) {
                    // SOLD_OUT_MANUAL 이 아니면 OUT_OF_STOCK으로
                    if (m.getStockStatus() != MenuStatus.SOLD_OUT_MANUAL
                            && m.getStockStatus() != MenuStatus.OUT_OF_STOCK) {
                        m.changeStockStatus(MenuStatus.OUT_OF_STOCK);
                        changed++;
                    }
                }

                if (changed > 0) {
                    menuRepository.saveAll(menus);
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
}
