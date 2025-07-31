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




    public String save(StoreCreateDto storeCreateDto) {
        if (storeRepository.findByLoginId(storeCreateDto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 아이디 입니다.");
        }

        String encodedPassword = passwordEncoder.encode(storeCreateDto.getPassword());
        Store store = storeRepository.save(storeCreateDto.toEntity(encodedPassword));
        return store.getLoginId();
    }
}
