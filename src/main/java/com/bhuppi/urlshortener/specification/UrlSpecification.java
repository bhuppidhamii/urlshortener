package com.bhuppi.urlshortener.specification;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.bhuppi.urlshortener.model.Url;

public class UrlSpecification {
    public static Specification<Url> hasKeyword(String keyword) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("originalUrl")),
                "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Url> hasMinClicks(Long minClicks) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                root.get("clickCount"),
                minClicks);
    }

    public static Specification<Url> hasMaxClicks(Long maxClicks) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("clickCount"), maxClicks);
    }

    public static Specification<Url> hasCreatedAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("createdAt"), createdAfter);
    }

    public static Specification<Url> hasExpiresBefore(LocalDateTime expiresBefore) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("expiresAt"), expiresBefore);
    }

    public static Specification<Url> hasExpiresAfter(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(
                root.get("expiresAt"),
                date);
    }
    

}
