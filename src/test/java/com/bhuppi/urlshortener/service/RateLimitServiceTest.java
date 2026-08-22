package com.bhuppi.urlshortener.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Test
    void shouldRejectRequestWhenRateLimiterRejects() {

        // Arrange
        when(redisRateLimiter.isAllowed(
                "rate:user:17",
                10,
                10.0 / 60_000)).thenReturn(false);

        // Act
        boolean result = rateLimitService.isAllowed(17L);

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldCallRateLimiterWithCorrectParameters() {

        // Arrange
        when(redisRateLimiter.isAllowed(
                "rate:user:17",
                10,
                10.0 / 60_000)).thenReturn(true);

        // Act
        rateLimitService.isAllowed(17L);

        // Assert
        verify(redisRateLimiter).isAllowed(
                "rate:user:17",
                10,
                10.0 / 60_000);
    }

    @Test
    void shouldCallRateLimiterOnlyOnce() {

        when(redisRateLimiter.isAllowed(
                "rate:user:17",
                10,
                10.0 / 60_000)).thenReturn(true);

        rateLimitService.isAllowed(17L);

        verify(redisRateLimiter, times(1)).isAllowed(
                "rate:user:17",
                10,
                10.0 / 60_000);
    }
}
