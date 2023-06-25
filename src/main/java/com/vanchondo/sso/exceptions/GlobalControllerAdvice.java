package com.vanchondo.sso.exceptions;

import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ControllerAdvice
public class GlobalControllerAdvice {
    private static String logMessage = "::handle:: Handle exception response for ex={}";

//    {
//        "timestamp": 1687361574931,
//        "status": 401,
//        "error": "Unauthorized",
//        "path": "/validate"
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handle(MethodArgumentNotValidException ex) {
        String errorValidation = Optional.ofNullable(ex)
            .map(MethodArgumentNotValidException::getBindingResult)
            .map(BindingResult::getFieldErrors)
            .map(fieldErrros -> fieldErrros.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "))
            )
            .orElse(Strings.EMPTY);
        log.warn("::handle:: error={}", errorValidation, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, errorValidation);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handle(BadRequestException ex) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handle(AuthenticationException ex) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handle(ConflictException ex) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }    

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handle(NotFoundException ex) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception ex) {
        log.info(logMessage, ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<String> buildResponse(HttpStatus status) { 
        return buildResponse(status, status.getReasonPhrase());
    }
    
    private ResponseEntity<String> buildResponse(HttpStatus status, String body) { 
        return ResponseEntity.status(status).body(body);
    }
}