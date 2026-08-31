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

import com.bhuppi.urlshortener.auth.entity.User;
import com.bhuppi.urlshortener.auth.repository.UserRepository;
import com.bhuppi.urlshortener.dto.PageRequestDto;
import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.dto.ShortenResponse;
import com.bhuppi.urlshortener.dto.search.SearchFilterRequest;
import com.bhuppi.urlshortener.enums.SortDirection;
import com.bhuppi.urlshortener.enums.SortField;
import com.bhuppi.urlshortener.exception.UrlExpiredException;
import com.bhuppi.urlshortener.exception.UrlNotFoundException;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.repository.UrlRepository;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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

        User user = User.builder().id(17L).email("user@example.com").username("user").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.google.com");
        request.setExpiresInDays(7);

        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act

        ShortenResponse response = urlService.shortenUrl(request, "user@example.com");

        // Assert

        assertNotNull(response);

        assertEquals("https://www.google.com", response.getOriginalUrl());

        assertNotNull(response.getShortCode());
        assertNotNull(response.getCreatedAt());

        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        // Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        ShortenRequest request = new ShortenRequest();
        request.setOriginalUrl("https://www.google.com");
        request.setExpiresInDays(7);

        // Act + Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class, () -> urlService.shortenUrl(request, "user@example.com"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldReturnCachedUrlWhenCacheHit() {

        // Arrange
        String shortCode = "abc12345";
        String cachedUrl = "https://www.google.com";

        when(redisService.get("url:" + shortCode)).thenReturn(cachedUrl);

        // Act
        String result = urlService.getOriginalUrl(shortCode);

        // Assert
        assertEquals(cachedUrl, result);

        verify(urlRepository, never()).findByShortCode(shortCode);

        verify(redisService).get("url:" + shortCode);

        verify(urlRepository).incrementClickCount(shortCode);
    }

    @Test
    void shouldFetchFromDatabaseAndCacheWhenCacheMiss() {

        // Arrange
        String shortCode = "abc12345";
        String originalUrl = "https://www.google.com";

        LocalDateTime now = LocalDateTime.now();

        Url url =
                Url.builder()
                        .shortCode(shortCode)
                        .originalUrl(originalUrl)
                        .createdAt(now)
                        .expiresAt(now.plusDays(1))
                        .clickCount(0L)
                        .build();

        // Redis MISS
        when(redisService.get("url:" + shortCode)).thenReturn(null);

        // PostgreSQL returns URL
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        // Act
        String result = urlService.getOriginalUrl(shortCode);

        // Assert
        assertEquals(originalUrl, result);

        verify(redisService).get("url:" + shortCode);

        verify(urlRepository).findByShortCode(shortCode);

        verify(redisService).set(eq("url:" + shortCode), eq(originalUrl), any(Duration.class));

        verify(urlRepository).incrementClickCount(shortCode);
    }

    @Test
    void shouldThrowExceptionWhenUrlIsExpired() {

        // Arrange
        String shortCode = "abc12345";

        LocalDateTime now = LocalDateTime.now();

        Url url =
                Url.builder()
                        .shortCode(shortCode)
                        .originalUrl("https://www.google.com")
                        .createdAt(now.minusDays(2))
                        .expiresAt(now.minusDays(1))
                        .clickCount(0L)
                        .build();

        when(redisService.get("url:" + shortCode)).thenReturn(null);

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        // Act + Assert
        UrlExpiredException exception =
                assertThrows(UrlExpiredException.class, () -> urlService.getOriginalUrl(shortCode));

        assertEquals("This URL has expired.", exception.getMessage());

        // Expired URL must never be cached
        verify(redisService, never()).set(anyString(), anyString(), any(Duration.class));

        // Expired URL must not count as a click
        verify(urlRepository, never()).incrementClickCount(shortCode);
    }

    @Test
    void shouldThrowExceptionWhenShortCodeDoesNotExist() {

        // Arrange
        String shortCode = "abc12345";

        when(redisService.get("url:" + shortCode)).thenReturn(null);

        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        // Act + Assert
        UrlNotFoundException exception =
                assertThrows(UrlNotFoundException.class, () -> urlService.getOriginalUrl(shortCode));

        assertEquals("Short code not found: " + shortCode, exception.getMessage());

        // URL must never be cached
        verify(redisService, never()).set(anyString(), anyString(), any(Duration.class));

        // Click count must not be incremented
        verify(urlRepository, never()).incrementClickCount(shortCode);
    }

    @Test
    void shouldReturnAnalyticsForUrlOwnedByUser() {

        // Arrange
        String shortCode = "abc12345";
        String email = "user@example.com";

        Url url =
                Url.builder()
                        .shortCode(shortCode)
                        .originalUrl("https://www.google.com")
                        .clickCount(10L)
                        .build();

        when(urlRepository.findByShortCodeAndUserEmail(shortCode, email)).thenReturn(Optional.of(url));

        // Act
        Url result = urlService.getAnalytics(shortCode, email);

        // Assert
        assertNotNull(result);

        assertEquals(shortCode, result.getShortCode());

        assertEquals("https://www.google.com", result.getOriginalUrl());

        assertEquals(10L, result.getClickCount());

        verify(urlRepository).findByShortCodeAndUserEmail(shortCode, email);
    }

    @Test
    void shouldRejectAnalyticsAccessWhenUrlDoesNotBelongToUser() {

        // Arrange
        String shortCode = "abc12345";
        String bobEmail = "bob@example.com";

        when(urlRepository.findByShortCodeAndUserEmail(shortCode, bobEmail))
                .thenReturn(Optional.empty());

        // Act + Assert
        UrlNotFoundException exception =
                assertThrows(
                        UrlNotFoundException.class, () -> urlService.getAnalytics(shortCode, bobEmail));

        assertEquals("URL not found: " + shortCode, exception.getMessage());

        // Verify ownership-aware repository query
        verify(urlRepository).findByShortCodeAndUserEmail(shortCode, bobEmail);
    }

    @Test
    void shouldSearchUrlsForAuthenticatedUser() {

        // Arrange
        SearchFilterRequest searchFilter = new SearchFilterRequest();
        PageRequestDto pageRequestDto = new PageRequestDto();

        when(urlRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        // Act
        Page<Url> result = urlService.searchUrls(
                searchFilter,
                pageRequestDto,
                "user@example.com"
        );

        // Assert
        assertNotNull(result);

        verify(urlRepository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    void shouldApplyPaginationAndSorting() {

        // Arrange
        SearchFilterRequest searchFilter = new SearchFilterRequest();

        PageRequestDto pageRequestDto = new PageRequestDto();
        pageRequestDto.setPage(2);
        pageRequestDto.setSize(5);
        pageRequestDto.setSortField(SortField.CREATED_AT);
        pageRequestDto.setDirection(SortDirection.ASC);

        when(urlRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        // Act
        urlService.searchUrls(
                searchFilter,
                pageRequestDto,
                "user@example.com"
        );

        // Assert
        verify(urlRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());

        Sort.Order order =
                capturedPageable.getSort().getOrderFor("createdAt");

        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }
}
