package com.vanchondo.sso.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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