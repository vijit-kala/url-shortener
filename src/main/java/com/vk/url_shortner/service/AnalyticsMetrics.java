package com.vk.url_shortner.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsMetrics {

    private final MeterRegistry registry;

    public AnalyticsMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordClick(String shortKey) {
        registry.counter("url.clicks", "shortKey", shortKey).increment();
    }

    public void recordCacheHit() {
        registry.counter("url.cache.hit").increment();
    }

    public void recordCacheMiss() {
        registry.counter("url.cache.miss").increment();
    }

    public void recordUrlCreated() {
        registry.counter("url.created").increment();
    }

    public void recordInvalidUrl() {
        registry.counter("url.invalid").increment();
    }


}
