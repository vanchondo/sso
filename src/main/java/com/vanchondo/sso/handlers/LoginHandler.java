package com.vanchondo.sso.handlers;

import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.services.ReactiveUserService;
import com.vanchondo.sso.utilities.Validate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Log4j2
public class LoginHandler {
  private final ReactiveUserService reactiveUserService;
  private final Validate validate;
  private final CaptchaValidatorService captchaValidatorService;

  public Mono<ServerResponse> handleRegister(ServerRequest request) {
    return request.bodyToMono(SaveUserDTO.class)
      .defaultIfEmpty(new SaveUserDTO())
      .flatMap(dto ->
        captchaValidatorService.validateCaptcha(dto, request.exchange())
            .flatMap(result -> validate.validate(dto))
      )
      .map(user -> (SaveUserDTO)user)
      .flatMap(user -> {
          return reactiveUserService.saveUser(user)
            .flatMap(dto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(dto));
      });
  }
}
