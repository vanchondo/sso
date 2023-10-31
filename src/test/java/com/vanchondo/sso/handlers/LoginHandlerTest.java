package com.vanchondo.sso.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.dtos.ErrorDTO;
import com.vanchondo.sso.dtos.security.CaptchaDTO;
import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.exceptions.GlobalErrorWebExceptionHandler;
import com.vanchondo.sso.filters.JwtFilter;
import com.vanchondo.sso.routers.LoginRouter;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.services.UserService;
import com.vanchondo.sso.utilities.Constants;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.RegexConstants;
import com.vanchondo.sso.utilities.TestConstants;
import com.vanchondo.sso.utilities.Validate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

import reactor.core.publisher.Mono;

@ExtendWith({SpringExtension.class})
@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {
  LoginRouter.class,
  LoginConfiguration.class,
  LoginHandler.class,
  Validate.class,
  JwtFilter.class,
  GlobalErrorWebExceptionHandler.class
})
public class LoginHandlerTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private CaptchaValidatorService captchaValidatorService;
  @MockBean
  private AuthenticationService authenticationService;
  @MockBean
  private UserService userService;
  @MockBean
  private LoginConfiguration loginConfiguration;

  @BeforeEach
  public void setup() {
    when(userService.saveUser(any(SaveUserDTO.class))).thenReturn(Mono.just(new UserDTO()));
    when(userService.validateUser(any(ValidateUserDTO.class))).thenReturn(Mono.just(true));
    when(userService.deleteUser(any(DeleteUserDTO.class), any(CurrentUserDTO.class))).thenReturn(Mono.just(true));
    when(captchaValidatorService.validateCaptcha(any(CaptchaDTO.class), any(ServerWebExchange.class))).thenReturn(Mono.just(true));
    when(loginConfiguration.getSecretKey()).thenReturn(TestConstants.TOKEN_SECRET_KEY);
    when(loginConfiguration.getUnsecuredUrls()).thenReturn(ObjectFactory.createUnsecureUrls());
    when(authenticationService.login(any(LoginDTO.class))).thenReturn(Mono.just(ObjectFactory.createTokenDTO()));
  }

  @Test
  public void testRegisterWhenRequiredParametersAreNotIncluded() {
    SaveUserDTO invalidDto = ObjectFactory.createSaveUserDTOWithInvalidProperties();
    webTestClient.post()
      .uri("/register")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(invalidDto), SaveUserDTO.class)
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody(ErrorDTO.class)
      .value(response -> {
        assertNotNull(response);
        assertFalse(CollectionUtils.isEmpty(response.getMessages()));
        assertEquals(3, response.getMessages().size());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getMessages().contains("Username not valid min=6, max=29"));
        assertTrue(response.getMessages().contains("Email not valid"));
        assertTrue(response.getMessages().contains("Password not valid, min=6, max=50"));
      });
  }

  @Test
  public void testRegisterWhenSuccess() {
    SaveUserDTO dto = ObjectFactory.createSaveUserDTO();
    webTestClient.post()
      .uri("/register")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(dto), SaveUserDTO.class)
      .exchange()
      .expectStatus().isCreated()
      .expectBody(UserDTO.class)
      .value(Assertions::assertNotNull);
  }

  @Test
  public void testRegisterWhenSuccessSpecialEmail() {
    SaveUserDTO dto = ObjectFactory.createSaveUserDTO();
    dto.setEmail("Dickens.8aaf0a8820b5@hotmail.com");
    webTestClient.post()
      .uri("/register")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(dto), SaveUserDTO.class)
      .exchange()
      .expectStatus().isCreated()
      .expectBody(UserDTO.class)
      .value(Assertions::assertNotNull);
  }

  @Test
  public void testRegisterWhenSuccessSpecialUsername() {
    SaveUserDTO dto = ObjectFactory.createSaveUserDTO();
    dto.setUsername("Michale.Schiller");
    dto.setPassword("password");
    dto.setEmail("Orlando.Prosacco22@gmail.com");
    webTestClient.post()
      .uri("/register")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(dto), SaveUserDTO.class)
      .exchange()
      .expectStatus().isCreated()
      .expectBody(UserDTO.class)
      .value(Assertions::assertNotNull);
  }

  @Test
  public void testHandleRegex() {
    webTestClient.get()
      .uri("/regex")
      .exchange()
      .expectStatus().isOk()
      .expectBody(Map.class)
      .value(response -> {
        assertNotNull(response);
        assertEquals(3, response.size());
        assertEquals(RegexConstants.USERNAME_REGEX, response.get("USERNAME_REGEX"));
        assertEquals(RegexConstants.PASSWORD_REGEX, response.get("PASSWORD_REGEX"));
        assertEquals(RegexConstants.EMAIL_REGEX, response.get("EMAIL_REGEX"));
      });
  }

  @Test
  public void testHandleCurrentUserWhenAuthorizationTokenIsNotIncluded() {
    webTestClient.get()
      .uri("/currentUser")
      .exchange()
      .expectStatus()
      .isUnauthorized();
  }

  @Test
  public void testHandleCurrentUserWhenAuthorizationTokenIsInvalid() {
    webTestClient.get()
      .uri("/currentUser")
      .header(HttpHeaders.AUTHORIZATION,"Bearer invalid token")
      .exchange()
      .expectStatus()
      .isUnauthorized();
  }

  @Test
  public void testHandleCurrentUser() {
    TokenDTO tokenDto = ObjectFactory.createTokenDTO();
    webTestClient.get()
      .uri("/currentUser")
      .header(HttpHeaders.AUTHORIZATION, Constants.BEARER_VALUE + tokenDto.getToken())
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(CurrentUserDTO.class)
      .value(response -> {
        assertNotNull(response);
        assertEquals(TestConstants.USERNAME, response.getUsername());
        assertEquals(TestConstants.EMAIL, response.getEmail());
      });
  }

  @Test
  public void testValidateWhenSuccess() {
    ValidateUserDTO dto = ObjectFactory.createValidateUserDto();
    webTestClient.post()
      .uri("/validate")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(dto), ValidateUserDTO.class)
      .exchange()
      .expectStatus().isOk();
  }

  @Test
  public void testLoginWhenSuccess() {
    LoginDTO dto = ObjectFactory.createLoginDto();
    webTestClient.post()
      .uri("/login")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(dto), LoginDTO.class)
      .exchange()
      .expectStatus().isOk()
      .expectBody(TokenDTO.class)
      .value(Assertions::assertNotNull);
  }

  @Test
  public void testDeleteUserWhenSuccess() {
    TokenDTO tokenDto = ObjectFactory.createTokenDTO();
    DeleteUserDTO dto = ObjectFactory.createDeleteUserDto();
    webTestClient.method(HttpMethod.DELETE)
      .uri("/user")
      .header(HttpHeaders.AUTHORIZATION, Constants.BEARER_VALUE + tokenDto.getToken())
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(dto), SaveUserDTO.class)
      .exchange()
      .expectStatus().isOk();
  }

  @Test
  public void testDeleteUserWhenFail() {
    when(userService.deleteUser(any(DeleteUserDTO.class), any(CurrentUserDTO.class))).thenReturn(Mono.just(false));
    TokenDTO tokenDto = ObjectFactory.createTokenDTO();
    DeleteUserDTO dto = ObjectFactory.createDeleteUserDto();
    webTestClient.method(HttpMethod.DELETE)
      .uri("/user")
      .header(HttpHeaders.AUTHORIZATION, Constants.BEARER_VALUE + tokenDto.getToken())
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(dto), SaveUserDTO.class)
      .exchange()
      .expectStatus().isBadRequest();
  }

}
