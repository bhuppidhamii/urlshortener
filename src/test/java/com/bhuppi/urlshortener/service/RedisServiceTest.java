package com.bhuppi.urlshortener.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private RedisService redisService;

  @Test
  void shouldReturnNullWhenRedisGetFails() {

    // Arrange
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    when(valueOperations.get("url:abc12345")).thenThrow(new RuntimeException("Redis unavailable"));

    // Act
    String result = redisService.get("url:abc12345");

    // Assert
    assertNull(result);
  }
}
