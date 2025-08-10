package com.beyond.jellyorder.domain.store.service;

import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.store.dto.StoreLoginReqDTO;
import com.beyond.jellyorder.domain.store.dto.StoreCreateDTO;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor

public class StoreService {
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessVerificationService businessVerificationService;

    /* Store 회원가입 Serivce */
    public UUID save(StoreCreateDTO dto) {
        // 0) 입력 정규화
        final String loginId = dto.getLoginId();
        final String bNo = normalizeBizNo(dto.getBusinessNumber());

        // 1) 중복검사
        if (storeRepository.findByLoginId(loginId).isPresent()) {
            throw new DuplicateResourceException("이미 가입된 아이디 입니다. " + loginId);
        }
        // 메서드명이 잘못되어 있으면 Repository에서 findByBusinessNumber로 정정 권장
        if (storeRepository.findBybusinessNumber(bNo).isPresent()) {
            throw new DuplicateResourceException("이미 가입된 사업자등록번호 입니다. " + bNo);
        }

        // 2) 국세청 진위 + 상태 확인 (비정상 시 IllegalArgumentException 발생)
        businessVerificationService.verify(bNo, dto.getStartDate(), dto.getOwnerName());

        // 3) 저장
        Store store = Store.builder()
                .loginId(loginId)
                .storeName(dto.getStoreName())
                .businessNumber(bNo)
                .ownerName(dto.getOwnerName())
                .ownerEmail(dto.getOwnerEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        storeRepository.save(store);
        return store.getId();
    }

    private String normalizeBizNo(String raw) {
        return raw == null ? null : raw.replaceAll("-", "").trim();
    }

    /* Store 로그인 Service*/
    public Store doLogin(StoreLoginReqDTO storeLoginReqDTO) {
        Store store = storeRepository.findByLoginId(storeLoginReqDTO.getLoginId())
                .orElseThrow(() -> new EntityNotFoundException("아이디!! 또는 비밀번호가 일치하지 않습니다.")) ; /* "로그인 정보가 일치하지 않습니다", 통일 예정 */
        if (!passwordEncoder.matches(storeLoginReqDTO.getPassword(), store.getPassword())){
            throw new IllegalArgumentException("아이디 또는 비밀번호!!가 일치하지 않습니다."); /* "로그인 정보가 일치하지 않습니다", 통일 예정 */
        }
        return store;


    }

}
