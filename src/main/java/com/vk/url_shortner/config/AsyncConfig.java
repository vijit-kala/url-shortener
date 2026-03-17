package com.vk.url_shortner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(name = "urlExecutor")
    public Executor urlExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
