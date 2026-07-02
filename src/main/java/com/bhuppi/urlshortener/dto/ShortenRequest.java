package com.bhuppi.urlshortener.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShortenRequest {
    @NotBlank(message = "URL Cannot be empty")
    @Pattern(regexp = "^(https?://)([\\w\\-]+\\.)+[\\w\\-]+(/.*)?$", message = "Invalid URL format. Must start with http:// or https://")
    private String originalUrl;

    @NotNull(message = "Exipry duration cannot be empty")
    @Min(value = 1, message = "Minimum expiry is 1 Day")
    @Max(value = 365, message = "Maximum expiry is 365 Days")
    private Integer expiresInDays;
}