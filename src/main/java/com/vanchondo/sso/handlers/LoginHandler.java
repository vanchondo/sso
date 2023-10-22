package com.vanchondo.sso.handlers;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.services.ReactiveUserService;
import com.vanchondo.sso.utilities.Constants;
import com.vanchondo.sso.utilities.RegexConstants;
import com.vanchondo.sso.utilities.Validate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Log4j2
public class LoginHandler {
  private final ReactiveUserService reactiveUserService;
  private final AuthenticationService authenticationService;
  private final Validate validate;
  private final CaptchaValidatorService captchaValidatorService;

  public Mono<ServerResponse> handleRegister(ServerRequest request) {
    log.info("::handleRegister::Entering method");
    return request.bodyToMono(SaveUserDTO.class)
      .defaultIfEmpty(new SaveUserDTO())
      .flatMap(dto ->
        captchaValidatorService.validateCaptcha(dto, request.exchange())
            .flatMap(result -> validate.validate(dto))
      )
      .map(user -> (SaveUserDTO)user)
      .flatMap(user ->
        reactiveUserService.saveUser(user)
          .flatMap(dto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(dto))
      );
  }

  public Mono<ServerResponse> handleRegex() {
    log.info("::handleRegex::Entering method");
    Map<String, String> regexMap = new HashMap<>();
    regexMap.put("USERNAME_REGEX", RegexConstants.USERNAME_REGEX);
    regexMap.put("PASSWORD_REGEX", RegexConstants.PASSWORD_REGEX);
    regexMap.put("EMAIL_REGEX", RegexConstants.EMAIL_REGEX);

    return ServerResponse.ok().bodyValue(regexMap);
  }

  public Mono<ServerResponse> handleCurrentUser(ServerRequest request) {
    log.info("::handleCurrentUser::Entering method");
    return request.attribute(Constants.CURRENT_USER_ATTRIBUTE)
      .map(currentUser -> (CurrentUserDTO)currentUser)
      .map(currentUser -> ServerResponse.ok().bodyValue(currentUser))
      .orElse(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
  }

  public Mono<ServerResponse> handleValidateUser(ServerRequest request) {
    log.info("::handleValidateUser::Entering method");
    return request.bodyToMono(ValidateUserDTO.class)
      .defaultIfEmpty(new ValidateUserDTO())
      .flatMap(validate::validate)
      .map(validateUser -> (ValidateUserDTO)validateUser)
      .flatMap(reactiveUserService::validateUser)
      .flatMap(result -> ServerResponse.ok().build());
  }

  public Mono<ServerResponse> handleLogin(ServerRequest request) {
    log.info("::handleLogin::Entering method");
    return request.bodyToMono(LoginDTO.class)
      .defaultIfEmpty(new LoginDTO())
      .flatMap(validate::validate)
      .map(login -> (LoginDTO)login)
      .flatMap(authenticationService::login)
      .flatMap(result -> ServerResponse.ok().bodyValue(result));
  }

  public Mono<ServerResponse> handleDeleteUser(ServerRequest request) {
    log.info("::handleDeleteUser::Entering method");
    return request.bodyToMono(DeleteUserDTO.class)
      .defaultIfEmpty(new DeleteUserDTO())
      .flatMap(dto ->
        captchaValidatorService.validateCaptcha(dto, request.exchange())
          .flatMap(result -> validate.validate(dto))
      )
      .map(currentUser -> (DeleteUserDTO)currentUser)
      .flatMap(user -> {
        CurrentUserDTO currentUser = (CurrentUserDTO) request.attribute(Constants.CURRENT_USER_ATTRIBUTE)
          .orElse(null);
        if (currentUser == null) {
          return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }
        return reactiveUserService.deleteUser(user, currentUser)
          .flatMap(result ->
            result
              ? ServerResponse.ok().build()
              : ServerResponse.badRequest().build()
          );
      });
  }
}
