package com.beyond.jellyorder.domain.ingredient.service;

import com.beyond.jellyorder.domain.ingredient.domain.Ingredient;
import com.beyond.jellyorder.domain.ingredient.dto.IngredientCreateReqDto;
import com.beyond.jellyorder.domain.ingredient.dto.IngredientCreateResDto;
import com.beyond.jellyorder.domain.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 새로운 식자재를 생성한다.
     * - 동일 매장(storeId) 내에서 이름(name)이 중복되면 예외를 발생시킨다.
     * - 생성된 식자재를 저장 후 응답 DTO로 변환하여 반환한다.
     *
     * @param reqDto 클라이언트로부터 전달받은 식자재 생성 요청 DTO
     * @return 생성된 식자재 정보를 담은 응답 DTO
     * @throws DuplicateIngredientNameException 동일 매장 내에 이름이 중복되는 경우
     */
    public IngredientCreateResDto create(IngredientCreateReqDto reqDto) {
        /*
            TODO: [2025-07-31 기준] storeId에 대한 실제 매장 UUID 유효성 검증 로직 추가 예정
            현재는 테스트 목적상 단순 UUID 문자열만 전달받고 있으나,
            향후 Store 도메인 및 StoreRepository가 구현되면 다음과 같은 형태의 검증 로직이 삽입될 예정이다:

                UUID authenticatedStoreId = getStoreIdFromAuthentication(); // 인증 객체로부터 추출
                if (!storeRepository.existsById(authenticatedStoreId)) {
                    throw new StoreNotFoundException(authenticatedStoreId); // 커스텀 예외 발생
                }

            이 검증은 인증된 사용자가 요청한 storeId가 실제로 존재하는 유효한 매장 식별자인지를 확인하여,
            권한이 없는 접근이나 잘못된 데이터 요청을 사전에 차단하는 데 목적이 있다.

            또한, 해당 검증은 서비스 전반에서 반복적으로 사용될 가능성이 높으므로,
            공통 검증 유틸리티 또는 Validator 클래스를 common 패키지 하위에 별도로 구성하는 방안에 대한 논의가 필요하다.
         */

        // 동일 매장 내에 동일한 이름의 식자재가 존재하는지 확인
        boolean exists = ingredientRepository.existsByStoreIdAndName(reqDto.getStoreId(), reqDto.getName());
        if (exists) {
            // 중복 식자재 존재 시 예외 발생
            throw new DuplicateIngredientNameException(reqDto.getName());
        }

        // 식자재 엔티티 생성
        Ingredient ingredient = Ingredient.builder()
                .storeId(reqDto.getStoreId())
                .name(reqDto.getName())
                .status(reqDto.getStatus())
                .build();

        // DB에 저장
        Ingredient saved = ingredientRepository.save(ingredient);

        // 저장된 식자재 정보를 응답 DTO로 변환하여 반환
        return IngredientCreateResDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .status(saved.getStatus())
                .build();
    }

    /**
     * 동일한 이름의 식자재가 이미 존재하는 경우 발생하는 예외 클래스.
     * - 매장 단위 중복 등록 방지 목적
     * - 전역 예외 처리 핸들러에서 처리 가능
     */
    public static class DuplicateIngredientNameException extends RuntimeException {
        public DuplicateIngredientNameException(String name) {
            super("이미 존재하는 식자재입니다: " + name);
        }
    }


}
