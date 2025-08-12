package com.beyond.jellyorder.common.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration

public class PasswordResetRedisConfig {
    @Bean
    public LettuceConnectionFactory passwordResetRedisCF(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port
    ) {
        RedisStandaloneConfiguration passwordResetRedisConfig = new RedisStandaloneConfiguration(host, port);
        passwordResetRedisConfig.setDatabase(3); // Redis index 3번 설정
        return new LettuceConnectionFactory(passwordResetRedisConfig);
    }

    @Bean(name = "passwordResetRedisTemplate")
    public RedisTemplate<String, String> passwordResetRedisTemplate(
            LettuceConnectionFactory passwordResetRedisCF
    ) {
        RedisTemplate<String, String> t = new RedisTemplate<>();
        t.setConnectionFactory(passwordResetRedisCF);
        t.afterPropertiesSet();
        return t;
    }
}

