package com.bhuppi.urlshortener.service;

import org.springframework.stereotype.Service;

import com.bhuppi.urlshortener.infrastructure.redis.RedisRateLimiter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final int CAPACITY = 10;

    private static final double REFILL_RATE = 10.0 / 60_000;

    private final RedisRateLimiter redisRateLimiter;

    public boolean isAllowed(Long userId) {

        String key = "rate:user:" + userId;

        return redisRateLimiter.isAllowed(
                key,
                CAPACITY,
                REFILL_RATE);
    }
}