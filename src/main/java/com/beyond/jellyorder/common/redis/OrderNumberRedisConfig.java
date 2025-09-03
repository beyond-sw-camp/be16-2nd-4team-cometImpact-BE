package com.beyond.jellyorder.common.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class OrderNumberRedisConfig {

    private final RedisProperties props;

    public OrderNumberRedisConfig(RedisProperties props) {
        this.props = props;
    }

    // 채번 DB
    @Bean(name = "orderNumber")
    public RedisConnectionFactory orderNumberConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(props.getHost());
        configuration.setPort(props.getPort());
        configuration.setDatabase(4);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean(name = "orderNumberTemplate")
    public RedisTemplate<String, String> orderNumberTemplate(
            @Qualifier("orderNumber") RedisConnectionFactory redisConnectionFactory
    ) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }





}
