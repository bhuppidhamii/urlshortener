package com.bhuppi.urlshortener.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bhuppi.urlshortener.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private ResponseEntity<ErrorResponse> buildErrorResponse(
                        HttpStatus status,
                        String message) {

                ErrorResponse error = ErrorResponse.builder()
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .message(message)
                                .timestamp(LocalDateTime.now())
                                .build();

                return ResponseEntity
                                .status(status)
                                .body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidationErrors(
                        MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        @ExceptionHandler(UrlNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUrlNotFound(
                        UrlNotFoundException ex) {

                return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        @ExceptionHandler(UrlExpiredException.class)
        public ResponseEntity<ErrorResponse> handleUrlExpired(
                        UrlExpiredException ex) {

                return buildErrorResponse(HttpStatus.GONE, ex.getMessage());
        }

        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistsException ex) {

                return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        BadCredentialsException ex) {

                return buildErrorResponse(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid email or password.");
        }

        @ExceptionHandler(RateLimitServiceUnavailableException.class)
        public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitServiceUnavailableException ex) {
                return buildErrorResponse(
                                HttpStatus.TOO_MANY_REQUESTS,
                                "Too many Requestx");
        }
}