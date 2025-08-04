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
public class SseRedisConfig {
    private final RedisProperties props;

    public SseRedisConfig(RedisProperties props) {
        this.props = props;
    }
    // Redis 채널로부터 수신된 메시지를 처리할 리스너 등록
    @Bean
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
        return new MessageListenerAdapter(sseAlarmService, "onMessage"); // onMessage() 호출
    }

    // Redis PubSub 리스너 컨테이너 등록
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("order-channel")); // 원하는 채널명
        return container;
    }

    // redis pub/sub을 위한 연결객체 생성
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory sseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(props.getHost());
        configuration.setPort(props.getPort());

        return (new LettuceConnectionFactory(configuration));
    }

    // ssePubSub 템플릿 객체
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, Object> sseRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return template;
    }

}
