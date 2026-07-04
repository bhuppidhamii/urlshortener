package com.bhuppi.urlshortener.service;

import com.bhuppi.urlshortener.dto.ShortenRequest;
import com.bhuppi.urlshortener.exception.UrlExpiredException;
import com.bhuppi.urlshortener.exception.UrlNotFoundException;
import com.bhuppi.urlshortener.model.Url;
import com.bhuppi.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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
                .orElseThrow(() ->
                new UrlNotFoundException("Short code not found: " + shortCode));

        if (LocalDateTime.now().isAfter(url.getExpiresAt())) {
            throw new UrlExpiredException("This URL has expired.");
        }
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();

    }
}