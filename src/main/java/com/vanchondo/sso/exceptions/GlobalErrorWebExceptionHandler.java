package com.vanchondo.sso.exceptions;

import com.vanchondo.security.exception.AuthenticationException;
import com.vanchondo.sso.dtos.ErrorDTO;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

  private static final String logMessage = "::handle:: Handle exception response for errorMessage={}";

  public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
    ApplicationContext applicationContext,
    ServerCodecConfigurer serverCodecConfigurer) {
    super(errorAttributes, new WebProperties.Resources(), applicationContext);
    super.setMessageWriters(serverCodecConfigurer.getWriters());
    super.setMessageReaders(serverCodecConfigurer.getReaders());
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
    log.error("::handle::Error occurred. ",throwable);
    return super.handle(exchange, throwable);
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    Throwable error = getError(request);
    ServerWebExchange exchange = request.exchange();

    if (error instanceof ConstraintViolationException) {
      return handle((ConstraintViolationException)error, exchange);
    }
    else if (error instanceof ReCaptchaInvalidException) {
      return handle((ReCaptchaInvalidException)error, exchange);
    }
    else if (error instanceof BadRequestException) {
      return handle((BadRequestException)error, exchange);
    }
    else if (error instanceof AuthenticationException) {
      return handle((AuthenticationException)error, exchange);
    }
    else if (error instanceof NotFoundException) {
      return handle((NotFoundException)error, exchange);
    }
    else if (error instanceof ConflictException) {
      return handle((ConflictException)error, exchange);
    }
    else {
      return handle((Exception)error, exchange);
    }
  }

  public static Mono<ServerResponse> handle(ConstraintViolationException ex, ServerWebExchange exchange) {
    List<String> messages = Optional.ofNullable(ex)
      .map(ConstraintViolationException::getConstraintViolations)
      .map(violations -> violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toList())
      )
      .orElse(Collections.emptyList());

    log.warn(logMessage, messages, ex);

    return buildResponse(HttpStatus.BAD_REQUEST, messages, exchange);
  }

  public static Mono<ServerResponse> handle(ReCaptchaInvalidException ex, ServerWebExchange exchange) {
    log.warn(logMessage, ex.getMessage(), ex);

    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
  }

  public static Mono<ServerResponse> handle(BadRequestException ex, ServerWebExchange exchange) {
    log.warn(logMessage, ex.getMessage(), ex);

    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
  }

  public static Mono<ServerResponse> handle(AuthenticationException ex, ServerWebExchange exchange) {
    log.warn(logMessage, ex.getMessage(), ex);

    return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), exchange);
  }

  public static Mono<ServerResponse> handle(NotFoundException ex, ServerWebExchange exchange) {
    log.warn(logMessage, ex.getMessage(), ex);

    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
  }

  public static Mono<ServerResponse> handle(ConflictException ex, ServerWebExchange exchange) {
    log.warn(logMessage, ex.getMessage(), ex);

    return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), exchange);
  }

  public static Mono<ServerResponse> handle(Exception ex, ServerWebExchange exchange) {
    log.info(logMessage, ex.getMessage(), ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exchange);
  }

  private static  Mono<ServerResponse> buildResponse(HttpStatus status, ServerWebExchange exchange) {
    return buildResponse(status.getReasonPhrase(), status, null, exchange);
  }

  private static  Mono<ServerResponse> buildResponse(HttpStatus status, String message, ServerWebExchange exchange) {
    return buildResponse(status.getReasonPhrase(), status, Collections.singletonList(message), exchange);
  }

  private static Mono<ServerResponse> buildResponse(HttpStatus status, List<String> messages, ServerWebExchange exchange) {
    return buildResponse(status.getReasonPhrase(), status, messages, exchange);
  }

  private  static  Mono<ServerResponse> buildResponse(String error, HttpStatus status, List<String> messages, ServerWebExchange exchange) {
    return ServerResponse.status(status).bodyValue(new ErrorDTO(error, status.value(), getRequestURI(exchange), messages));
  }

  private static String getRequestURI(ServerWebExchange exchange) {
    return exchange.getRequest().getURI().toString();
  }
}
