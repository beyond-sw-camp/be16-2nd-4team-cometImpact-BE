// src/main/java/com/beyond/jellyorder/common/redis/SseRedisConfig.java
package com.beyond.jellyorder.common.redis;

import com.beyond.jellyorder.domain.menu.sse.MenuStatusRedisSubscriber;
import com.beyond.jellyorder.domain.menu.sse.MenuStatusToSseBridge;
import com.beyond.jellyorder.domain.sseRequest.service.SseAlarmService;
import com.beyond.jellyorder.domain.sseRequest.sse.RequestRedisSubscriber;
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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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

    @Bean(name = "requestListenerAdapter")
    public MessageListenerAdapter requestListenerAdapter(RequestRedisSubscriber requestRedisSubscriber) {
        return new MessageListenerAdapter(requestRedisSubscriber, "onMessage");
    }

    @Bean(name = "requestRedisMessageListenerContainer")
    public RedisMessageListenerContainer requestRedisMessageListenerContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
            @Qualifier("requestListenerAdapter") MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("request-channel"));
        return container;
    }

    // ===== 2) tablet-menu-status:* → MenuStatusRedisSubscriber =====
    @Bean(name = "menuStatusBridgeListenerAdapter")
    public MessageListenerAdapter menuStatusBridgeListenerAdapter(MenuStatusToSseBridge bridge) {
        return new MessageListenerAdapter(bridge, "onMessage");
    }

    @Bean(name = "menuStatusBridgeContainer")
    public RedisMessageListenerContainer menuStatusBridgeContainer(
            @Qualifier("ssePubSub") RedisConnectionFactory cf,
            @Qualifier("menuStatusBridgeListenerAdapter") MessageListenerAdapter adapter) {
        var c = new RedisMessageListenerContainer();
        c.setConnectionFactory(cf);
        c.addMessageListener(adapter, new PatternTopic("tablet-menu-status:*"));
        return c;
    }

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
        // 채널(topic)은 String, 메시지 value도 "String(JSON)"
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        var keySer = new StringRedisSerializer();
        var jsonSer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySer);
        template.setValueSerializer(jsonSer);
        template.setHashKeySerializer(keySer);
        template.setHashValueSerializer(jsonSer);

        template.setEnableDefaultSerializer(false);
        template.afterPropertiesSet();
        return template;
    }
}
