package com.vk.url_shortner.service;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class RateLimitedService {

    private final RateLimiter limiter = RateLimiter.create(5.0);

    public boolean tryAcquire() {
        return limiter.tryAcquire();
    }
}
