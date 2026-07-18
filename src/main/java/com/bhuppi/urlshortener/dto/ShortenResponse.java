package com.bhuppi.urlshortener.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ShortenResponse {
    private String originalUrl;
    private LocalDateTime createdAt;
    private String shortCode;
}
