package com.beyond.jellyorder.common.auth;

import com.beyond.jellyorder.domain.store.entity.Store;
import com.beyond.jellyorder.domain.store.repository.StoreRepository;
import com.beyond.jellyorder.domain.storetable.entity.StoreTable;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class AuthService {
    private final StoreAuthRtValidator storeAuthRtValidator;
    private final StoreTableAuthRtValidator storeTableAuthRtValidator;

    /* Store RefreshToken 검증 */
    public Store validateStoreRt(String storeRefreshToken) {
        return storeAuthRtValidator.validate(storeRefreshToken);
    }

    /* StoreTable RefreshToken 검증 */
    public StoreTable validateStoreTableRt(String storeTableRefreshToken) {
        return storeTableAuthRtValidator.validate(storeTableRefreshToken);
    }

}
