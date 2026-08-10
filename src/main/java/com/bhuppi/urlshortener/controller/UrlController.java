package com.bhuppi.urlshortener.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhuppi.urlshortener.dto.AnalyticsResponse;
import com.bhuppi.urlshortener.dto.PageRequestDto;
import com.bhuppi.urlshortener.dto.SearchResponse;
import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.dto.ShortenResponse;
import com.bhuppi.urlshortener.dto.search.SearchFilterRequest;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "URL Management", description = "Operations for creating, searching, and redirecting shortened URLs.")
public class UrlController {
        private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

        private final UrlService urlService;

        @Operation(summary = "Create a shortened URL", description = "Creates a new shortened URL from the provided original URL.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Short URL created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid URL provided")
        })
        @PostMapping("/shorten")
        public ResponseEntity<ShortenResponse> shortenUrl(
                        @Valid @RequestBody ShortenRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {
                logger.info("Received request to shorten URL");
                return ResponseEntity.ok(
                                urlService.shortenUrl(request, userDetails.getUsername()));
        }

        // protected route
        @GetMapping("/s/{shortCode}")
        public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
                String originalUrl = urlService.getOriginalUrl(shortCode);
                return ResponseEntity.status(302)
                                .location(URI.create(originalUrl))
                                .<Void>build();
        }

        @GetMapping("/analytics/{shortCode}")
        public ResponseEntity<AnalyticsResponse> getAnalytics(
                        @PathVariable String shortCode,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Url url = urlService.getAnalytics(shortCode, userDetails.getUsername());

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
                        SearchFilterRequest searchFilter, PageRequestDto pageRequestDto,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Page<Url> urls = urlService.searchUrls(searchFilter, pageRequestDto, userDetails.getUsername());

                Page<SearchResponse> response = urls.map(url -> SearchResponse.builder()
                                .originalUrl(url.getOriginalUrl())
                                .shortCode(url.getShortCode())
                                .clickCount(url.getClickCount())
                                .build());

                return ResponseEntity.ok(response);
        }
}