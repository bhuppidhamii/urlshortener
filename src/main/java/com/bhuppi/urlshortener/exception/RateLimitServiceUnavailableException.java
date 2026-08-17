package com.bhuppi.urlshortener.exception;

public class RateLimitServiceUnavailableException extends RuntimeException {

    public RateLimitServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}