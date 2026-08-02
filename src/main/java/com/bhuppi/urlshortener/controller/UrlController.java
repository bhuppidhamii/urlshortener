package com.bhuppi.urlshortener.controller;

import com.bhuppi.urlshortener.dto.AnalyticsResponse;
import com.bhuppi.urlshortener.dto.PageRequestDto;
import com.bhuppi.urlshortener.dto.SearchResponse;
import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.dto.ShortenResponse;
import com.bhuppi.urlshortener.dto.search.SearchFilterRequest;
import com.bhuppi.urlshortener.enums.SortDirection;
import com.bhuppi.urlshortener.enums.SortField;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@Tag(name = "URL Management", description = "Operations for creating, searching, and redirecting shortened URLs.")
public class UrlController {
    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);
    

    @Autowired
    private UrlService urlService;

    @Operation(summary = "Create a shortened URL", description = "Creates a new shortened URL from the provided original URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid URL provided")
    })
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        logger.info("Received request to shorten URL");
        return ResponseEntity.ok(urlService.shortenUrl(request));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);
        return ResponseEntity.status(302)
                .location(URI.create(originalUrl))
                .<Void>build();
    }

    @GetMapping("/analytics/{shortCode}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable String shortCode) {

        Url url = urlService.getAnalytics(shortCode);

        AnalyticsResponse response = AnalyticsResponse.builder()
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SearchResponse>> searchUrls(
            SearchFilterRequest searchFilter, PageRequestDto pageRequestDto) {

        Page<Url> urls = urlService.searchUrls(searchFilter, pageRequestDto);

        Page<SearchResponse> response = urls.map(url -> SearchResponse.builder()
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .clickCount(url.getClickCount())
                .build());

        return ResponseEntity.ok(response);
    }
}