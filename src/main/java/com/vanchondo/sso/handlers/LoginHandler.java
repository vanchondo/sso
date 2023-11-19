package com.vanchondo.sso.handlers;

import com.vanchondo.security.dto.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.entities.PictureEntity;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.services.UserService;
import com.vanchondo.sso.utilities.Constants;
import com.vanchondo.sso.utilities.LogUtil;
import com.vanchondo.sso.utilities.RegexConstants;
import com.vanchondo.sso.utilities.Validate;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Log4j2
public class LoginHandler {
  private final UserService userService;
  private final AuthenticationService authenticationService;
  private final Validate validate;
  private final CaptchaValidatorService captchaValidatorService;

  public Mono<ServerResponse> handleRegister(ServerRequest request) {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
    return request.bodyToMono(SaveUserDTO.class)
      .defaultIfEmpty(new SaveUserDTO())
      .flatMap(dto ->
        captchaValidatorService.validateCaptcha(dto, request.exchange())
            .flatMap(result -> validate.validate(dto))
      )
      .map(user -> (SaveUserDTO)user)
      .flatMap(user ->
        userService.saveUser(user)
          .flatMap(dto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(dto))
      );
  }

  public Mono<ServerResponse> handleRegex() {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
    Map<String, String> regexMap = new HashMap<>();
    regexMap.put("USERNAME_REGEX", RegexConstants.USERNAME_REGEX);
    regexMap.put("PASSWORD_REGEX", RegexConstants.PASSWORD_REGEX);
    regexMap.put("EMAIL_REGEX", RegexConstants.EMAIL_REGEX);

    return ServerResponse.ok().bodyValue(regexMap);
  }

  public Mono<ServerResponse> handleCurrentUser(ServerRequest request) {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
    return request.attribute(Constants.CURRENT_USER_ATTRIBUTE)
      .map(currentUser -> (CurrentUserDTO)currentUser)
      .map(currentUser -> ServerResponse.ok().bodyValue(currentUser))
      .orElse(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
  }

  public Mono<ServerResponse> handleValidateUser(ServerRequest request) {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
    return request.bodyToMono(ValidateUserDTO.class)
      .defaultIfEmpty(new ValidateUserDTO())
      .flatMap(validate::validate)
      .map(validateUser -> (ValidateUserDTO)validateUser)
      .flatMap(userService::validateUser)
      .flatMap(result -> ServerResponse.ok().build());
  }

  public Mono<ServerResponse> handleLogin(ServerRequest request) {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
    return request.bodyToMono(LoginDTO.class)
      .defaultIfEmpty(new LoginDTO())
      .flatMap(validate::validate)
      .map(login -> (LoginDTO)login)
      .flatMap(authenticationService::login)
      .flatMap(result -> ServerResponse.ok().bodyValue(result));
  }

  public Mono<ServerResponse> handleDeleteUser(ServerRequest request) {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
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
        return userService.deleteUser(user, currentUser)
          .flatMap(result ->
            result
              ? ServerResponse.ok().build()
              : ServerResponse.badRequest().build()
          );
      });
  }

  public Mono<ServerResponse> handleUpdateUser(ServerRequest request) {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
    return request.bodyToMono(UpdateUserDTO.class)
      .defaultIfEmpty(new UpdateUserDTO())
      .flatMap(validate::validate)
      .map(user -> (UpdateUserDTO)user)
      .flatMap(user -> {
        CurrentUserDTO currentUser = (CurrentUserDTO) request.attribute(Constants.CURRENT_USER_ATTRIBUTE)
          .orElse(null);
        if (currentUser == null) {
          return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userService.updateUser(user, currentUser)
          .flatMap(result -> ServerResponse.ok().build());
      });
  }

  public Mono<ServerResponse> handleProfilePicture(ServerRequest request) {
    String methodName = LogUtil.getMethodName(new Object(){});
    log.info("{}Entering method", methodName);
    return request.attribute(Constants.CURRENT_USER_ATTRIBUTE)
      .map(currentUser -> (CurrentUserDTO)currentUser)
      .map(userService::getProfilePicture)
      .orElse(Mono.just(new PictureEntity()))
      .flatMap(profilePicture -> {
        String mimeType = Optional.ofNullable(profilePicture.getType())
          .orElse(MediaType.IMAGE_JPEG_VALUE);
        byte[] picture = profilePicture.getPicture();
        return ArrayUtils.isEmpty(picture)
          ? ServerResponse.noContent().build()
          : ServerResponse.ok()
            .contentType(MediaType.valueOf(mimeType))
            .bodyValue(picture);
      });

  }
}
