package com.beyond.jellyorder.domain.category.service;

import com.beyond.jellyorder.domain.category.dto.CategoryCreateReqDto;
import com.beyond.jellyorder.domain.category.dto.CategoryCreateResDto;
import com.beyond.jellyorder.domain.category.repository.CategoryRepository;
import com.beyond.jellyorder.domain.category.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카테고리 관련 비즈니스 로직을 담당하는 서비스 클래스.
 * 카테고리 생성 요청을 처리하며, 동일 storeId 내 중복된 카테고리명 존재 여부를 검증한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 새로운 카테고리를 생성한다.
     * 동일 storeId 내에서 name이 중복될 경우 예외를 발생시킨다.
     *
     * @param categoryCreateReqDto 클라이언트로부터 전달받은 카테고리 생성 요청 DTO
     * @return 생성된 카테고리 정보를 담은 응답 DTO
     * @throws DuplicateCategoryNameException 동일한 이름의 카테고리가 이미 존재하는 경우 발생
     */


    public CategoryCreateResDto create(CategoryCreateReqDto categoryCreateReqDto) {
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

        // 중복 카테고리 이름 존재 여부 확인
        boolean exists = categoryRepository.existsByStoreIdAndName(categoryCreateReqDto.getStoreId(), categoryCreateReqDto.getName());
        if (exists) {
            // 중복되면 커스텀 런타임 예외 발생 (공통 예외처리기로 위임)
            throw new DuplicateCategoryNameException(categoryCreateReqDto.getName());
        }

        // 신규 카테고리 객체 생성 (Builder 사용)
        Category newCategory = Category.builder()
                .storeId(categoryCreateReqDto.getStoreId())
                .name(categoryCreateReqDto.getName())
                .description(categoryCreateReqDto.getDescription())
                .build();

        // DB에 저장
        Category saved = categoryRepository.save(newCategory);

        // 저장된 엔티티로 응답 DTO 구성 후 반환
        return new CategoryCreateResDto(saved.getId(), saved.getName(), saved.getDescription());
    }

    /**
     * 동일한 storeId 범위 내에 이미 동일한 name 값을 갖는 카테고리가 존재하는 경우 발생하는 예외이다.
     * 서비스 계층에서 중복 카테고리 등록을 사전에 방지하기 위한 목적으로 사용된다.
     * 현재는 CategoryService의 내부 static 클래스로 정의되어 있으나,
     * 향후 예외 처리 일관성을 위해 공통 예외 처리 패키지로의 분리가 검토가 필요하다.
     */
    public static class DuplicateCategoryNameException extends RuntimeException {
        public DuplicateCategoryNameException(String name) {
            super("이미 존재하는 카테고리입니다: " + name);
        }
    }
}
