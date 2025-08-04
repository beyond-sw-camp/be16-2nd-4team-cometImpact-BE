package com.beyond.jellyorder.config;

import com.beyond.jellyorder.common.auth.RedisProperties;
import com.beyond.jellyorder.domain.sseRequest.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class AuthRedisConfig {
    private final RedisProperties props;

    public AuthRedisConfig(RedisProperties props) {
        this.props = props;
    }

    /* RefreshToken Redis에 저장하기 위한 Factory, 0번에 설정하였으나 추후 변경 가능 합니다! */
    @Bean
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(props.getHost());
        configuration.setPort(props.getPort());
        configuration.setDatabase(0);

        return new LettuceConnectionFactory(configuration);
    }

    /* RefreshToken RedisTemplate */
    @Bean
    @Qualifier("rtInventory")
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }

    // 개수 관리 redis
    @Bean
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(props.getHost());
        configuration.setPort(props.getPort());
        configuration.setDatabase(1);

        return (new LettuceConnectionFactory(configuration));
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisTemplate<String, Object> stockTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }
}