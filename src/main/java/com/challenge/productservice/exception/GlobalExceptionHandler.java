package com.challenge.productservice.exception;

import com.challenge.productservice.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNotFound(ProductNotFoundException exception,
                                                          ServerWebExchange exchange) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), List.of(), exchange);
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public Mono<ResponseEntity<ApiError>> handleConflict(DuplicateSkuException exception,
                                                          ServerWebExchange exchange) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), List.of(), exchange);
    }

    @ExceptionHandler(ProductValidationException.class)
    public Mono<ResponseEntity<ApiError>> handleBusinessValidation(ProductValidationException exception,
                                                                    ServerWebExchange exchange) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of(), exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleBeanValidation(WebExchangeBindException exception,
                                                                ServerWebExchange exchange) {
        List<String> details = exception.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + Optional.ofNullable(error.getDefaultMessage())
                        .orElse("invalid value"))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                details,
                exchange
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleUnexpected(Exception exception,
                                                            ServerWebExchange exchange) {
        LOGGER.error("Unexpected error processing {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected internal error",
                List.of(),
                exchange
        );
    }

    private Mono<ResponseEntity<ApiError>> buildResponse(HttpStatus status,
                                                          String message,
                                                          List<String> details,
                                                          ServerWebExchange exchange) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                details
        );

        return Mono.just(ResponseEntity.status(status).body(body));
    }
}
