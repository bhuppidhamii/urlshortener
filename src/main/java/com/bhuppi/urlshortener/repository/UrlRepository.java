package com.bhuppi.urlshortener.repository;

import com.bhuppi.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);

    Page<Url> findByOriginalUrlContainingIgnoreCase(
            String keyword,
            Pageable pageable);
}