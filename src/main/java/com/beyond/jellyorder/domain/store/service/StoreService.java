package com.beyond.jellyorder.domain.store.service;

import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.store.dto.StoreLoginIdFindDTO;
import com.beyond.jellyorder.domain.store.dto.StoreLoginReqDTO;
import com.beyond.jellyorder.domain.store.dto.StoreCreateDTO;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.sqm.EntityTypeException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    // 0) 입력 정규화, 사업자번호 하이픈 '-' 제거
    private String normalizeBizNo(String raw) {
        return raw == null ? null : raw.replaceAll("-", "").trim();
    }
    public UUID save(StoreCreateDTO dto) {

        final String loginId = dto.getLoginId();
        final String bNo = normalizeBizNo(dto.getBusinessNumber());

        // 1) 중복검사
        if (storeRepository.findByLoginId(loginId).isPresent()) {
            throw new DuplicateResourceException("이미 가입된 아이디 입니다. " + loginId);
        }
        if (storeRepository.findByBusinessNumber(bNo).isPresent()) {
            throw new DuplicateResourceException("이미 가입된 사업자등록번호 입니다. " + bNo);
        }

//        // 2) 국세청 등록된 사업자번호 진위 + 상태 확인 (비정상 시 IllegalArgumentException 발생)
        /* 사업자번호 진위 확인 때문에 회원가입이 막힙니다, 실구현시 주석 제거 후 사용 예정 */
//        businessVerificationService.verify(bNo, dto.getStartDate(), dto.getOwnerName());

        // 3) 저장
        Store store = Store.builder()
                .loginId(loginId)
                .storeName(dto.getStoreName())
                .businessNumber(bNo)
                .ownerName(dto.getOwnerName())
                .ownerEmail(dto.getOwnerEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .businessOpenedAt(LocalDateTime.now())
                .build();

        storeRepository.save(store);
        return store.getId();
    }

    /* Store 로그인 Service*/
    public Store doLogin(StoreLoginReqDTO storeLoginReqDTO) {
        Store store = storeRepository.findByLoginId(storeLoginReqDTO.getLoginId())
                .orElseThrow(() -> new EntityNotFoundException("로그인 정보가 일치하지 않습니다.")) ;
        if (!passwordEncoder.matches(storeLoginReqDTO.getPassword(), store.getPassword())){
            throw new IllegalArgumentException("로그인 정보가 일치하지 않습니다.");
        }
        return store;
    }

    /* Store LoginId 존재 여부 확인 */
    public boolean existsLoginId(String loginId) {
        return storeRepository.findByLoginId(loginId).isPresent();
    }

    /* Store BusinessNumber 존재 여부 확인 */
    public boolean existsBusinessNumber(String businessNumber) {
        return storeRepository.findByBusinessNumber(businessNumber).isPresent();
    }

    /* Store Login Id 찾기 */
    public String findLoginId(StoreLoginIdFindDTO storeLoginIdFindDTO) {
        String ownerName = storeLoginIdFindDTO.getOwnerName();
        String businessNumber = storeLoginIdFindDTO.getBusinessNumber();

        Store store = storeRepository.findByOwnerNameAndBusinessNumber(ownerName, businessNumber)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 사업자 번호입니다." + businessNumber));

        String loginID = store.getLoginId();
        return loginID;
    }

}
