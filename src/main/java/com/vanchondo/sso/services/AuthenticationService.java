package com.vanchondo.sso.services;

import com.vanchondo.security.dto.TokenDTO;
import com.vanchondo.security.dto.UserInfoForTokenDTO;
import com.vanchondo.security.exception.AuthenticationException;
import com.vanchondo.security.service.SecurityService;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@AllArgsConstructor
public class AuthenticationService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final SecurityService securityService;

  /**
   * Logs in a user and returns their access token.
   *
   * @param loginDTO the login details of the user
   * @return a Mono containing the user's access token or an error if there was a problem logging in
   * @throws AuthenticationException if there is a problem authenticating the user
   */
  public Mono<TokenDTO> login(LoginDTO login) throws AuthenticationException {
    String username = login.getUsername();
    String password = login.getPassword();

    return userService
        .findUserEntityByUsername(username)
        .defaultIfEmpty(new UserEntity())
        .flatMap(
            user -> {
              if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                log.warn("::login::Invalid email or password for username={}", username);
                return Mono.error(new AuthenticationException("Email or password incorrect"));
              }
              if (!user.isActive()) {
                log.warn("::login::User is not active, username={}", username);
                return Mono.error(new AuthenticationException("User is not active"));
              }
              UserInfoForTokenDTO userInfo = new UserInfoForTokenDTO();
              userInfo.setEmail(user.getEmail());
              userInfo.setUsername(user.getUsername());
              return Mono.just(securityService.generateToken(userInfo));
            })
        .onErrorResume(
            NotFoundException.class,
            ex -> {
              log.warn("::login:: User not found", ex);
              return Mono.error(new AuthenticationException("Email or password incorrect"));
            });
  }
}
