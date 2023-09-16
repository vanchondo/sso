package com.vanchondo.sso.exceptions;

import com.vanchondo.sso.dtos.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ControllerAdvice
public class GlobalControllerAdvice {
    private static final String logMessage = "::handle:: Handle exception response for ex={}";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handle(MethodArgumentNotValidException ex, ServerWebExchange exchange) {
        List<String> messages = Optional.ofNullable(ex)
            .map(MethodArgumentNotValidException::getBindingResult)
            .map(BindingResult::getFieldErrors)
            .map(fieldError -> fieldError .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList())
            )
            .orElse(Collections.emptyList());
        log.warn("::handle:: error={}", messages, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, messages, exchange);
    }

    @ExceptionHandler(ReCaptchaInvalidException.class)
    public ResponseEntity<ErrorDTO> handle(ReCaptchaInvalidException ex, ServerWebExchange exchange) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDTO> handle(BadRequestException ex, ServerWebExchange exchange) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> handle(AuthenticationException ex, ServerWebExchange exchange) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), exchange);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorDTO> handle(ConflictException ex, ServerWebExchange exchange) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), exchange);
    }    

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDTO> handle(NotFoundException ex, ServerWebExchange exchange) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handle(Exception ex, ServerWebExchange exchange) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exchange);
    }

    private ResponseEntity<ErrorDTO> buildResponse(HttpStatus status, ServerWebExchange exchange) {
        return buildResponse(status.getReasonPhrase(), status, null, exchange);
    }

    private ResponseEntity<ErrorDTO> buildResponse(HttpStatus status, String message, ServerWebExchange exchange) {
        return buildResponse(status.getReasonPhrase(), status, Collections.singletonList(message), exchange);
    }

    private ResponseEntity<ErrorDTO> buildResponse(HttpStatus status, List<String> messages, ServerWebExchange exchange) {
        return buildResponse(status.getReasonPhrase(), status, messages, exchange);
    }

    private ResponseEntity<ErrorDTO> buildResponse(String error, HttpStatus status, List<String> messages, ServerWebExchange exchange) {
        return ResponseEntity.status(status).body(new ErrorDTO(error, status.value(), getRequestURI(exchange), messages));
    }

    private static String getRequestURI(ServerWebExchange exchange) {
        return exchange.getRequest().getURI().toString();
    }
}