package com.bhuppi.urlshortener.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bhuppi.urlshortener.infrastructure.redis.RedisRateLimiter;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RedisRateLimiter redisRateLimiter;

    @InjectMocks
    private RateLimitService rateLimitService;

    @Test
    void shouldAllowRequestWhenRateLimiterAllows() {

        // Arrange
        when(redisRateLimiter.isAllowed(
                "rate:user:17",
                10,
                10.0 / 60_000)).thenReturn(true);

        // Act
        boolean result = rateLimitService.isAllowed(17L);

        // Assert
        assertTrue(result);
    }
}
