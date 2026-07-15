package com.bhuppi.urlshortener.controller;

import com.bhuppi.urlshortener.dto.AnalyticsResponse;
import com.bhuppi.urlshortener.dto.PageRequestDto;
import com.bhuppi.urlshortener.dto.SearchResponse;
import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.dto.search.SearchFilterRequest;
import com.bhuppi.urlshortener.enums.SortDirection;
import com.bhuppi.urlshortener.enums.SortField;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<Url> shortenUrl(@Valid @RequestBody ShortenRequest request) {
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