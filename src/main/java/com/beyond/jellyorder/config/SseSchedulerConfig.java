// src/main/java/com/beyond/jellyorder/common/config/SseSchedulerConfig.java
package com.beyond.jellyorder.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SseSchedulerConfig {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler s = new ThreadPoolTaskScheduler();
        s.setPoolSize(2);
        s.setThreadNamePrefix("sse-heartbeat-");
        s.initialize();
        return s;
    }
}
