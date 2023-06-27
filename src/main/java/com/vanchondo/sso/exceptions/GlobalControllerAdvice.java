package com.vanchondo.sso.exceptions;

import com.vanchondo.sso.dtos.ErrorDTO;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Log4j2
@ControllerAdvice
public class GlobalControllerAdvice {
    private static final String logMessage = "::handle:: Handle exception response for ex={}";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handle(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> messages = Optional.ofNullable(ex)
            .map(MethodArgumentNotValidException::getBindingResult)
            .map(BindingResult::getFieldErrors)
            .map(fieldError -> fieldError .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList())
            )
            .orElse(Collections.emptyList());
        log.warn("::handle:: error={}", messages, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, messages, request);
    }

    @ExceptionHandler(ReCaptchaInvalidException.class)
    public ResponseEntity<ErrorDTO> handle(ReCaptchaInvalidException ex, WebRequest request) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDTO> handle(BadRequestException ex, WebRequest request) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> handle(AuthenticationException ex, WebRequest request) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorDTO> handle(ConflictException ex, WebRequest request) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }    

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDTO> handle(NotFoundException ex, WebRequest request) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handle(Exception ex, WebRequest request) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorDTO> buildResponse(HttpStatus status, WebRequest request) {
        return buildResponse(status.getReasonPhrase(), status, null, request);
    }

    private ResponseEntity<ErrorDTO> buildResponse(HttpStatus status, String message, WebRequest request) {
        return buildResponse(status.getReasonPhrase(), status, Collections.singletonList(message), request);
    }

    private ResponseEntity<ErrorDTO> buildResponse(HttpStatus status, List<String> messages, WebRequest request) {
        return buildResponse(status.getReasonPhrase(), status, messages, request);
    }

    private ResponseEntity<ErrorDTO> buildResponse(String error, HttpStatus status, List<String> messages, WebRequest request) {
        return ResponseEntity.status(status).body(new ErrorDTO(error, status.value(), getRequestURI(request), messages));
    }

    private static String getRequestURI(WebRequest request) {
        return ((ServletWebRequest)request).getRequest().getRequestURI().toString();
    }
}