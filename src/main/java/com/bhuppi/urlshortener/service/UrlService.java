package com.bhuppi.urlshortener.service;

import com.bhuppi.urlshortener.dto.PageRequestDto;
import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.dto.ShortenResponse;
import com.bhuppi.urlshortener.dto.search.SearchFilterRequest;

import com.bhuppi.urlshortener.exception.UrlExpiredException;
import com.bhuppi.urlshortener.exception.UrlNotFoundException;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.repository.UrlRepository;
import com.bhuppi.urlshortener.specification.UrlSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    public ShortenResponse shortenUrl(ShortenRequest request) {
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(UUID.randomUUID().toString().substring(0, 8));
        url.setClickCount(0L);

        LocalDateTime now = LocalDateTime.now();
        url.setCreatedAt(now);
        url.setExpiresAt(now.plusDays(request.getExpiresInDays()));
        Url savedUrl = urlRepository.save(url);

        return ShortenResponse.builder()
                .shortCode(savedUrl.getShortCode())
                .originalUrl(savedUrl.getOriginalUrl())
                .createdAt(savedUrl.getCreatedAt())
                .build();
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

    public Page<Url> searchUrls(SearchFilterRequest searchFilter, PageRequestDto pageRequestDto) {

        Sort sort = Sort.by(
                pageRequestDto.getDirection().toSpringDirection(),
                pageRequestDto.getSortField().getFieldName());

        Specification<Url> specification = Specification.unrestricted();

        if (searchFilter.getKeyword() != null && !searchFilter.getKeyword().isBlank()) {
            specification = specification.and(
                    UrlSpecification.hasKeyword(searchFilter.getKeyword()));
        }
        if (searchFilter.getMinClicks() != null) {
            specification = specification.and(UrlSpecification.hasMinClicks(searchFilter.getMinClicks()));
        }
        if (searchFilter.getMaxClicks() != null) {
            specification = specification.and(
                    UrlSpecification.hasMaxClicks(searchFilter.getMaxClicks()));
        }
        if (searchFilter.getCreatedAfter() != null) {
            specification = specification.and(
                    UrlSpecification.hasCreatedAfter(searchFilter.getCreatedAfter()));
        }
        if (searchFilter.getExpiresBefore() != null) {
            specification = specification.and(UrlSpecification.hasExpiresBefore(searchFilter.getExpiresBefore()));
        }
        LocalDateTime now = LocalDateTime.now();

        if (Boolean.TRUE.equals(searchFilter.getActive())) {
            specification = specification.and(
                    UrlSpecification.hasExpiresAfter(now));
        }

        if (Boolean.FALSE.equals(searchFilter.getActive())) {
            specification = specification.and(
                    UrlSpecification.hasExpiresBefore(now));
        }

        Pageable pageable = PageRequest.of(pageRequestDto.getPage(), pageRequestDto.getSize(), sort);
        return urlRepository.findAll(
                specification,
                pageable);
    }
}