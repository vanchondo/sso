package com.vanchondo.sso.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.dtos.ErrorDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.handlers.LoginHandler;
import com.vanchondo.sso.repositories.ReactiveUserRepository;
import com.vanchondo.sso.services.ReactiveUserService;
import com.vanchondo.sso.utilities.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.CollectionUtils;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LoginHandler.class, LoginConfiguration.class})
@WebFluxTest
public class LoginRoutesTest {
  @Autowired
  private WebTestClient webTestClient;
  @MockBean
  ReactiveUserService reactiveUserService;
  @MockBean
  private ReactiveUserRepository reactiveUserRepository;
  @MockBean
  private Validate validate;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    when(reactiveUserService.saveUser(any(SaveUserDTO.class))).thenReturn(Mono.just(new UserDTO()));
  }

  @Test
  public void testRegisterWhenRequiredParametersAreNotIncluded() throws Exception {
    webTestClient.post()
      .uri("/register")
      .body(Mono.just(new SaveUserDTO()), SaveUserDTO.class)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody(ErrorDTO.class)
      .value(response -> {
        assertNotNull(response);
        assertFalse(CollectionUtils.isEmpty(response.getMessages()));
        assertEquals(4, response.getMessages().size());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getError());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getMessages().contains("Username is required"));
        assertTrue(response.getMessages().contains("Email is required"));
        assertTrue(response.getMessages().contains("Password is required"));
      });
  }
}
