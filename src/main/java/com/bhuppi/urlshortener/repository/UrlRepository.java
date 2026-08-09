package com.bhuppi.urlshortener.repository;

import com.bhuppi.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UrlRepository extends JpaRepository<Url, Long>,
        JpaSpecificationExecutor<Url> {

    Optional<Url> findByShortCode(String shortCode);

}