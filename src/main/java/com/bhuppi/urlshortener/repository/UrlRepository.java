package com.bhuppi.urlshortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.bhuppi.urlshortener.model.Url;

public interface UrlRepository extends JpaRepository<Url, Long>,
        JpaSpecificationExecutor<Url> {

    Optional<Url> findByShortCodeAndUserEmail(
            String shortCode,
            String email);

    Optional<Url> findByShortCode(String shortCode);

}