package com.vanchondo.sso.handlers;

import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.exceptions.GlobalErrorWebExceptionHandler;
import com.vanchondo.sso.services.ReactiveUserService;
import com.vanchondo.sso.utilities.Validate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Log4j2
public class LoginHandler {
  private final ReactiveUserService reactiveUserService;
  private final Validate validate;

//  @ValidateCaptcha
  public Mono<ServerResponse> handleRegister(ServerRequest request) {
    return request.bodyToMono(SaveUserDTO.class)
      .defaultIfEmpty(new SaveUserDTO())
      .flatMap(validate::validate)
      .map(user -> (SaveUserDTO)user)
      .flatMap(user -> {
          return reactiveUserService.saveUser(user)
            .flatMap(dto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(dto));
      })
      .onErrorResume(ConstraintViolationException.class, error ->
        GlobalErrorWebExceptionHandler.handle(error, request.exchange())
      );
  }
}
