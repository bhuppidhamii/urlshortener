package com.bhuppi.urlshortener.dto;

import com.bhuppi.urlshortener.enums.SortDirection;
import com.bhuppi.urlshortener.enums.SortField;

import lombok.Data;

@Data
public class PageRequestDto {
    private int page = 0;
    private int size = 10;
    private SortField sortField = SortField.CREATED_AT;
    private SortDirection direction = SortDirection.DESC;
}
