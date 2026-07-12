package com.bhuppi.urlshortener.enums;

public enum SortField {
    CLICK_COUNT("clickCount"),
    CREATED_AT("createdAt"),
    EXPIRES_AT("expiresAt");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

}
