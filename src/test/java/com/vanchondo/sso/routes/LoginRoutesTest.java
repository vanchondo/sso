package com.vanchondo.sso.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vanchondo.sso.dtos.ErrorDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.handlers.LoginHandler;
import com.vanchondo.sso.services.ReactiveUserService;
import com.vanchondo.sso.utilities.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import reactor.core.publisher.Mono;

//@ExtendWith(SpringExtension.class)
//@WebFluxTest(excludeAutoConfiguration = {
//  ReactiveSecurityAutoConfiguration.class,
//  ReactiveUserDetailsServiceAutoConfiguration.class
//})
//@ContextConfiguration(classes = {
//  LoginRoutes.class,
//  LoginHandler.class,
//  JwtFilter.class,
//  LoginConfiguration.class
//})
public class LoginRoutesTest {
//  @Autowired
  private WebTestClient webTestClient;
  private ReactiveUserService reactiveUserService;
  private Validate validate;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    reactiveUserService = mock(ReactiveUserService.class);
    validate = mock(Validate.class);
    when(validate.validate(any())).thenReturn(Mono.just(new SaveUserDTO()));
    LoginHandler loginHandler = new LoginHandler(reactiveUserService, validate);
    RouterFunction<?> routes = new LoginRoutes().loginRoutes(loginHandler);
    webTestClient = WebTestClient.bindToRouterFunction(routes).build();
    when(reactiveUserService.saveUser(any(SaveUserDTO.class))).thenReturn(Mono.just(new UserDTO()));
  }

  @Test
  public void testRegisterWhenRequiredParametersAreNotIncluded() throws Exception {
    Set<ConstraintViolation<Object>> violations = new HashSet<>();
//    ConstraintViolation<Object> message = mock(ConstraintViolation.class);
//    when(message.getMessage()).thenReturn("Username is required");
//    violations.add(message);
    when(validate.validate(any(SaveUserDTO.class))).thenThrow(new ConstraintViolationException(violations));
    webTestClient.post()
      .uri("/register")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(new SaveUserDTO()), SaveUserDTO.class)
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody(ErrorDTO.class)
      .value(response -> {
        assertNotNull(response);
//        assertFalse(CollectionUtils.isEmpty(response.getMessages()));
        assertEquals(0, response.getMessages().size());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getMessages().contains("Username is required"));
      });
  }
}
