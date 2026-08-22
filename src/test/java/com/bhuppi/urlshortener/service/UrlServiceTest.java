package com.bhuppi.urlshortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bhuppi.urlshortener.auth.entity.User;
import com.bhuppi.urlshortener.auth.repository.UserRepository;
import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.dto.ShortenResponse;
import com.bhuppi.urlshortener.exception.UrlExpiredException;
import com.bhuppi.urlshortener.exception.UrlNotFoundException;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.repository.UrlRepository;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

        @Mock
        private UrlRepository urlRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private RedisService redisService;

        @InjectMocks
        private UrlService urlService;

        @Test
        void shouldShortenUrlForExistingUser() {

                // Arrange

                User user = User.builder()
                                .id(17L)
                                .email("user@example.com")
                                .username("user")
                                .build();

                when(userRepository.findByEmail("user@example.com"))
                                .thenReturn(Optional.of(user));

                ShortenRequest request = new ShortenRequest();
                request.setOriginalUrl("https://www.google.com");
                request.setExpiresInDays(7);

                when(urlRepository.save(any(Url.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act

                ShortenResponse response = urlService.shortenUrl(
                                request,
                                "user@example.com");

                // Assert

                assertNotNull(response);

                assertEquals(
                                "https://www.google.com",
                                response.getOriginalUrl());

                assertNotNull(response.getShortCode());
                assertNotNull(response.getCreatedAt());

                verify(urlRepository).save(any(Url.class));
        }

        @Test
        void shouldThrowExceptionWhenUserDoesNotExist() {

                // Arrange
                when(userRepository.findByEmail("user@example.com"))
                                .thenReturn(Optional.empty());

                ShortenRequest request = new ShortenRequest();
                request.setOriginalUrl("https://www.google.com");
                request.setExpiresInDays(7);

                // Act + Assert
                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> urlService.shortenUrl(
                                                request,
                                                "user@example.com"));

                assertEquals("User not found", exception.getMessage());
        }

        @Test
        void shouldReturnCachedUrlWhenCacheHit() {

                // Arrange
                String shortCode = "abc12345";
                String cachedUrl = "https://www.google.com";

                when(redisService.get("url:" + shortCode))
                                .thenReturn(cachedUrl);

                // Act
                String result = urlService.getOriginalUrl(shortCode);

                // Assert
                assertEquals(cachedUrl, result);

                verify(urlRepository, never())
                                .findByShortCode(shortCode);

                verify(redisService).get("url:" + shortCode);

                verify(urlRepository).incrementClickCount(shortCode);
        }

        @Test
        void shouldFetchFromDatabaseAndCacheWhenCacheMiss() {

                // Arrange
                String shortCode = "abc12345";
                String originalUrl = "https://www.google.com";

                LocalDateTime now = LocalDateTime.now();

                Url url = Url.builder()
                                .shortCode(shortCode)
                                .originalUrl(originalUrl)
                                .createdAt(now)
                                .expiresAt(now.plusDays(1))
                                .clickCount(0L)
                                .build();

                // Redis MISS
                when(redisService.get("url:" + shortCode))
                                .thenReturn(null);

                // PostgreSQL returns URL
                when(urlRepository.findByShortCode(shortCode))
                                .thenReturn(Optional.of(url));

                // Act
                String result = urlService.getOriginalUrl(shortCode);

                // Assert
                assertEquals(originalUrl, result);

                verify(redisService).get("url:" + shortCode);

                verify(urlRepository).findByShortCode(shortCode);

                verify(redisService).set(
                                eq("url:" + shortCode),
                                eq(originalUrl),
                                any(Duration.class));

                verify(urlRepository).incrementClickCount(shortCode);
        }

        @Test
        void shouldThrowExceptionWhenUrlIsExpired() {

                // Arrange
                String shortCode = "abc12345";

                LocalDateTime now = LocalDateTime.now();

                Url url = Url.builder()
                                .shortCode(shortCode)
                                .originalUrl("https://www.google.com")
                                .createdAt(now.minusDays(2))
                                .expiresAt(now.minusDays(1))
                                .clickCount(0L)
                                .build();

                when(redisService.get("url:" + shortCode))
                                .thenReturn(null);

                when(urlRepository.findByShortCode(shortCode))
                                .thenReturn(Optional.of(url));

                // Act + Assert
                UrlExpiredException exception = assertThrows(
                                UrlExpiredException.class,
                                () -> urlService.getOriginalUrl(shortCode));

                assertEquals(
                                "This URL has expired.",
                                exception.getMessage());

                // Expired URL must never be cached
                verify(redisService, never())
                                .set(
                                                anyString(),
                                                anyString(),
                                                any(Duration.class));

                // Expired URL must not count as a click
                verify(urlRepository, never())
                                .incrementClickCount(shortCode);
        }

        @Test
        void shouldThrowExceptionWhenShortCodeDoesNotExist() {

                // Arrange
                String shortCode = "abc12345";

                when(redisService.get("url:" + shortCode))
                                .thenReturn(null);

                when(urlRepository.findByShortCode(shortCode))
                                .thenReturn(Optional.empty());

                // Act + Assert
                UrlNotFoundException exception = assertThrows(
                                UrlNotFoundException.class,
                                () -> urlService.getOriginalUrl(shortCode));

                assertEquals(
                                "Short code not found: " + shortCode,
                                exception.getMessage());

                // URL must never be cached
                verify(redisService, never())
                                .set(
                                                anyString(),
                                                anyString(),
                                                any(Duration.class));

                // Click count must not be incremented
                verify(urlRepository, never())
                                .incrementClickCount(shortCode);
        }
}