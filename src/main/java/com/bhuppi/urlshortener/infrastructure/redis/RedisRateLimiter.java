package com.bhuppi.urlshortener.infrastructure.redis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.bhuppi.urlshortener.exception.RateLimitServiceUnavailableException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RedisRateLimiter.class);
    private final DefaultRedisScript<Long> rateLimitScript = new DefaultRedisScript<>(
            """
                    local key = KEYS[1]

                    local capacity = tonumber(ARGV[1])
                    local refillRate = tonumber(ARGV[2])
                    local now = tonumber(ARGV[3])

                    local tokens = redis.call('HGET', key, 'tokens')
                    local lastRefillTime = redis.call('HGET', key, 'lastRefillTime')

                    if tokens == false then
                        tokens = capacity
                        lastRefillTime = now
                    else
                        tokens = tonumber(tokens)
                        lastRefillTime = tonumber(lastRefillTime)

                        local elapsed = now - lastRefillTime

                        tokens = math.min(
                            capacity,
                            tokens + (elapsed * refillRate)
                        )
                    end

                    local allowed = 0

                    if tokens >= 1 then
                        tokens = tokens - 1
                        allowed = 1
                    end

                    redis.call(
                        'HSET',
                        key,
                        'tokens',
                        tokens,
                        'lastRefillTime',
                        now
                    )

                    return allowed
                    """,
            Long.class);

    public boolean isAllowed(
            String key,
            int capacity,
            double refillRate) {
        try {
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    List.of(key),
                    String.valueOf(capacity),
                    String.valueOf(refillRate),
                    String.valueOf(System.currentTimeMillis()));

            return result != null && result == 1L;

        } catch (Exception e) {
            logger.error("Redis rate limiter unavailable", e);

            throw new RateLimitServiceUnavailableException(
                    "Rate limiting service is temporarily unavailable", e);
        }

    }
}