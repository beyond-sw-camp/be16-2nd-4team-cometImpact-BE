package com.beyond.jellyorder.common.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class StoreAuthRedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    /* RefreshToken Redis에 저장하기 위한 Factory, 0번에 설정하였으나 추후 변경 가능 합니다! */
    /* Store (점주) RefreshToken Redis에 저장하기 위한 Factory */
    @Bean(name = "storeAuthRedisConnectionFactory")
    public RedisConnectionFactory storeAuthRedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);

        return new LettuceConnectionFactory(configuration);
    }

    /* RefreshToken RedisTemplate */
    /* Store (점주) RefreshToken RedisTemplate */
    @Bean(name = "storeRedisTemplate")
    public RedisTemplate<String, Object> storeAuthRedisTemplate(@Qualifier("storeAuthRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) { // 0번 팩토리 (Bean)싱글톤 객체로 주입 받겠다
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

}
