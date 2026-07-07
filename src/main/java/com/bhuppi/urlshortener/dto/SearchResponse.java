package com.bhuppi.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResponse {

    private String originalUrl;

    private String shortCode;

    private Long clickCount;

}