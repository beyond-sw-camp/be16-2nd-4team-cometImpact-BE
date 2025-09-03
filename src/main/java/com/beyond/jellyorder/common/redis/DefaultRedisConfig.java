package com.beyond.jellyorder.common.redis;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class DefaultRedisConfig {

    private final RedisProperties props;

    public DefaultRedisConfig(RedisProperties props) {
        this.props = props;
    }

    // 기본 Redis: 0번 / StoreAuthRedis: 1번 / StoreTableAuth: 2번 / Sse: 7번 / OrderNumberRedis: 3번
    /* 총 0, 1, 2, 7 사용중! */
    @Bean(name = "defaultRedisConnectionFactory")
    @Primary // 충돌 방지용, 이 factory를 기본으로 사용
    public RedisConnectionFactory defaultRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(props.getHost());
        config.setPort(props.getPort());
        config.setDatabase(0);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary // 충돌 방지용, 이 RedisTemplate이 기본으로 주입됨
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory defaultRedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(defaultRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}