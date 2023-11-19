package com.vanchondo.sso.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.vanchondo.security.configs.properties.LoginConfiguration;
import com.vanchondo.security.dto.UserInfoForTokenDTO;
import com.vanchondo.security.exception.AuthenticationException;
import com.vanchondo.security.service.SecurityService;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.NotFoundException;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith({SpringExtension.class})
public class AuthenticationServiceTest {
  @Mock
  private UserService userService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private LoginConfiguration loginConfiguration;
  @Mock
  private SecurityService securityService;
  @InjectMocks
  private AuthenticationService authenticationService;

  @BeforeEach
  public void setup() {
    when(loginConfiguration.getExpirationToken()).thenReturn(TestConstants.TOKEN_EXPIRATION);
    when(loginConfiguration.getIssuer()).thenReturn(TestConstants.TOKEN_ISSUER);
    when(loginConfiguration.getSecretKey()).thenReturn(TestConstants.TOKEN_SECRET_KEY);
    when(securityService.generateToken(any(UserInfoForTokenDTO.class))).thenReturn(ObjectFactory.createTokenDTO());
  }

  @Test
  public void testLoginWhenSuccess() {
    UserEntity user = ObjectFactory.createUserEntity();
    when(userService.findUserEntityByUsername(anyString())).thenReturn(Mono.just(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    LoginDTO loginDTO = ObjectFactory.createLoginDto();
    StepVerifier.create(authenticationService.login(loginDTO))
      .assertNext(Assertions::assertNotNull)
      .verifyComplete();
  }

  @Test
  public void testLoginWhenUserNotFound() {
    when(userService.findUserEntityByUsername(anyString())).thenReturn(Mono.error(new NotFoundException("Not found")));
    LoginDTO loginDTO = ObjectFactory.createLoginDto();

    StepVerifier.create(authenticationService.login(loginDTO))
      .expectError(AuthenticationException.class)
      .verify();
  }

  @Test
  public void testLoginWhenPasswordIsIncorrect() {
    UserEntity user = ObjectFactory.createUserEntity();
    when(userService.findUserEntityByUsername(anyString())).thenReturn(Mono.just(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    LoginDTO loginDTO = ObjectFactory.createLoginDto();

    StepVerifier.create(authenticationService.login(loginDTO))
      .expectError(AuthenticationException.class)
      .verify();
  }

  @Test
  public void testLoginWhenUserIsNotActive() {
    UserEntity user = ObjectFactory.createUserEntity();
    user.setActive(false);

    when(userService.findUserEntityByUsername(anyString())).thenReturn(Mono.just(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    LoginDTO loginDTO = ObjectFactory.createLoginDto();

    StepVerifier.create(authenticationService.login(loginDTO))
      .expectError(AuthenticationException.class)
      .verify();
  }
}
