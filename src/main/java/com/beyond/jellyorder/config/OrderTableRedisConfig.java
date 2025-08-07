package com.beyond.jellyorder.config;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class OrderTableRedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    /**
     * 주문 테이블 상태 캐싱 전용 RedisConnectionFactory
     */
    @Bean(name = "orderTableRedisConnectionFactory")
    public RedisConnectionFactory orderRedisConnectionFactory() {
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(host, port);
        cfg.setDatabase(8);  // DB index 8 사용
        return new LettuceConnectionFactory(cfg);
    }

    /**
     * 타입 정보를 포함하는 ObjectMapper를 사용하는 Serializer 등록
     */
    @Bean
    public GenericJackson2JsonRedisSerializer orderTableSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * 주문 테이블 상태 저장 전용 RedisTemplate
     */
    @Bean(name = "orderRedisTemplate")
    public RedisTemplate<String, Object> orderTableRedisTemplate(
            @Qualifier("orderTableRedisConnectionFactory") RedisConnectionFactory cf,
            GenericJackson2JsonRedisSerializer orderTableSerializer
    ) {
        RedisTemplate<String, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(orderTableSerializer);
        return tpl;
    }


    // 캐시

    /**
     * 기본 Redis 캐시 동작(Null 값 캐싱 비활성화, TTL 12시간)
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            GenericJackson2JsonRedisSerializer orderTableSerializer
    ) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(orderTableSerializer)
                );

    }

    /**
     * RedisCacheManager 설정: "tableStatuses" 캐시명 초기화
     */
    @Bean(name = "orderTableCacheManager")
    @Primary
    public CacheManager orderTableCacheManager(
            @Qualifier("orderTableRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration redisCacheConfiguration
    ) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(
                        Map.of("storeZones", redisCacheConfiguration, "zoneTables", redisCacheConfiguration
                        )
                )
                .build();
    }

}
