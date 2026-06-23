package com.bhuppi.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShortenRequest {
    @NotBlank(message = "URL Cannot be empty")
    @Pattern(
            regexp = "^(https?://)([\\w\\-]+\\.)+[\\w\\-]+(/.*)?$",
            message = "Invalid URL format. Must start with http:// or https://"
    )
    private String originalUrl;
}