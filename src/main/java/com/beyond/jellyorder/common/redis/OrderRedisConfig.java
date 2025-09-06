package com.beyond.jellyorder.common.redis;

import com.beyond.jellyorder.domain.order.service.OrderPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class OrderRedisConfig {

    private final RedisProperties props;

    public OrderRedisConfig(RedisProperties props) {
        this.props = props;
    }

    /// **------------ 주문번호 채번 redis 테이블 config 설정 ------------**
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

    /// **------------ 주문 redis pub/sub config 설정 ------------**
    // 주문 pub/sub
    @Bean(name = "orderPubSub")
    public RedisConnectionFactory orderPubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(props.getHost());
        configuration.setPort(props.getPort());
        return new LettuceConnectionFactory(configuration);
    }

    // publish 객체
    @Bean(name = "orderPubSubTemplate")
    public StringRedisTemplate stringRedisTemplate(
            @Qualifier("orderPubSub") RedisConnectionFactory redisConnectionFactory
    ) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    // subscribe 객체
    @Bean(name = "orderRedisMessageListenerContainer")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("orderPubSub") RedisConnectionFactory redisConnectionFactory,
            @Qualifier("orderMessageListenerAdapter") MessageListenerAdapter messageListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order")); // 주문 pub/sub이기 때문에 order라고 채널 네이밍
        return container;
    }

    // Redis에 수신된 메시지를 처리하는 객체 생성
    @Bean(name = "orderMessageListenerAdapter")
    public MessageListenerAdapter messageListenerAdapter(OrderPubSubService orderPubSubService) {
        // OrderPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정.
        return new MessageListenerAdapter(orderPubSubService, "onMessage");
    }


}
