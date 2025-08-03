package com.beyond.jellyorder.domain.store.service;

import com.beyond.jellyorder.common.exception.DuplicateResourceException;
import com.beyond.jellyorder.domain.store.dto.LoginRequestDto;
import com.beyond.jellyorder.domain.store.dto.StoreCreateDto;
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

    /* Store 회원가입 Serivce */
    public UUID save(StoreCreateDto storeCreateDto) {
        if (storeRepository.findByLoginId(storeCreateDto.getLoginId()).isPresent()) {
            Optional<Store> store = storeRepository.findByLoginId(storeCreateDto.getLoginId());
            String loginId = store.get().getLoginId();
            throw new DuplicateResourceException("이미 가입된 아이디 입니다. " + loginId);
        }
        if (storeRepository.findByRegisteredNumber(storeCreateDto.getRegisteredNumber()).isPresent()) {
            Optional<Store> store = storeRepository.findByRegisteredNumber(storeCreateDto.getRegisteredNumber());
            String registeredNumber = store.get().getRegisteredNumber();
            throw new DuplicateResourceException("이미 가입된 사업자등록번호 입니다. " + registeredNumber);
        }

        String encodedPassword = passwordEncoder.encode(storeCreateDto.getPassword());
        Store store = storeRepository.save(storeCreateDto.toEntity(encodedPassword));
        return store.getId(); /* 리턴값 UUID로 수정 완료, 주석 삭제 하고 사용하세요! */
    }

    public String doLogin(LoginRequestDto loginRequestDto) {
        Store store = storeRepository.findByLoginId(loginRequestDto.getLoginId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), store.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀립니다.");
        }
        return loginRequestDto.getLoginId(); // 나중에 At, Rt 리턴으로 수정 예정
    }

}
