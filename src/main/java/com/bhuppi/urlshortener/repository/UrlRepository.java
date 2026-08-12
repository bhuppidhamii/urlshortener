package com.bhuppi.urlshortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bhuppi.urlshortener.model.Url;

public interface UrlRepository extends JpaRepository<Url, Long>,
                JpaSpecificationExecutor<Url> {

        Optional<Url> findByShortCodeAndUserEmail(
                        String shortCode,
                        String email);

        Optional<Url> findByShortCode(String shortCode);

        @Modifying
        @Query("""
                UPDATE Url u
                SET u.clickCount = u.clickCount + 1
                WHERE u.shortCode = :shortCode
                        """)
        int incrementClickCount(@Param("shortCode") String shortCode);

}