package com.bhuppi.urlshortener.dto.search;

import java.time.LocalDateTime;

import lombok.Data;


@Data
public class SearchFilterRequest {
    private String keyword;
    private Long minClicks;
    private Long maxClicks;
    private LocalDateTime createdAfter;
    private LocalDateTime expiresBefore;
    private Boolean active;   
}