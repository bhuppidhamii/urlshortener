package com.bhuppi.urlshortener.service;

import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.enums.SortDirection;
import com.bhuppi.urlshortener.enums.SortField;
import com.bhuppi.urlshortener.exception.UrlExpiredException;
import com.bhuppi.urlshortener.exception.UrlNotFoundException;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    public Url shortenUrl(ShortenRequest request) {
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(UUID.randomUUID().toString().substring(0, 8));
        url.setClickCount(0L);

        LocalDateTime now = LocalDateTime.now();
        url.setCreatedAt(now);
        url.setExpiresAt(now.plusDays(request.getExpiresInDays()));

        return urlRepository.save(url);
    }

    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));

        if (LocalDateTime.now().isAfter(url.getExpiresAt())) {
            throw new UrlExpiredException("This URL has expired.");
        }
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public Url getAnalytics(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));
        return url;
    }

    public Page<Url> searchUrls(String keyword, int page, int size, SortField sortField, SortDirection direction) {

        Sort sort = Sort.by(
                direction.toSpringDirection(),
                sortField.getFieldName());

        Pageable pageable = PageRequest.of(page, size, sort);

        return urlRepository.findByOriginalUrlContainingIgnoreCase(
                keyword,
                pageable);
    }
}