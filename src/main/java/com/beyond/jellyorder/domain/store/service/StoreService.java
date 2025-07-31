package com.beyond.jellyorder.domain.store.service;

import com.beyond.jellyorder.domain.dto.StoreCreateDto;
import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor

public class StoreService {
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;


    public String save(StoreCreateDto storeCreateDto) { // Store 회원가입
        if (storeRepository.findByLoginId(storeCreateDto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 아이디 입니다.");
        }
        if (storeRepository.findByRegisteredNumber(storeCreateDto.getRegisteredNumber()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 사업자등록번호 입니다.");
        }
        if (storeRepository.findByOwnerEmail(storeCreateDto.getOwnerEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 사업자 이메일 입니다.");
        }

        String encodedPassword = passwordEncoder.encode(storeCreateDto.getPassword());
        Store store = storeRepository.save(storeCreateDto.toEntity(encodedPassword));
        return store.getLoginId();
    }
}
