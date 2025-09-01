// src/main/java/com/beyond/jellyorder/common/redis/SseRedisConfig.java
package com.beyond.jellyorder.common.redis;

import com.beyond.jellyorder.domain.menu.sse.MenuStatusRedisSubscriber;
import com.beyond.jellyorder.domain.sseRequest.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class SseRedisConfig {

    private final RedisProperties props;

    public SseRedisConfig(RedisProperties props) {
        this.props = props;
    }

    // ===== 1) 기존: request-channel → SseAlarmService =====
    @Bean(name = "messageListenerAdapter")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }

    @Bean(name = "redisMessageListenerContainer")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
            @Qualifier("messageListenerAdapter") MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("order-channel"));
        return container;
    }

    // ===== 2) tablet-menu-status:* → MenuStatusRedisSubscriber =====
    @Bean(name = "menuStatusListenerAdapter")
    public MessageListenerAdapter menuStatusListenerAdapter(MenuStatusRedisSubscriber subscriber) {
        // 바이트 그대로 넘겨서 subscriber가 직접 ObjectMapper로 파싱 (현재 구현 유지)
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean(name = "menuRedisMessageListenerContainer")
    public RedisMessageListenerContainer menuRedisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
            @Qualifier("menuStatusListenerAdapter") MessageListenerAdapter menuAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(menuAdapter, new PatternTopic("tablet-menu-status:*"));
        return container;
    }

    // ===== 공통: 연결/템플릿 =====
    @Bean(name = "ssePubSub")
    public RedisConnectionFactory sseFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(props.getHost());
        configuration.setPort(props.getPort());
        configuration.setDatabase(7);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean(name = "sseRedisTemplate")
    public RedisTemplate<String, Object> sseRedisTemplate(
            @Qualifier("ssePubSub") RedisConnectionFactory cf
    ) {
        // ⬅️ 채널(topic)은 String, 메시지 value도 "String(JSON)"로 보냅니다
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        StringRedisSerializer stringSer = new StringRedisSerializer();

        template.setKeySerializer(stringSer);
        template.setValueSerializer(stringSer);
        template.setHashKeySerializer(stringSer);
        template.setHashValueSerializer(stringSer);

        template.setEnableDefaultSerializer(false);
        template.afterPropertiesSet();
        return template;
    }
}
