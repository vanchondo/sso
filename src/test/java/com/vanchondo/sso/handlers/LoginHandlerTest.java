package com.vanchondo.sso.handlers;

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
import com.vanchondo.sso.exceptions.GlobalErrorWebExceptionHandler;
import com.vanchondo.sso.routes.LoginRoutes;
import com.vanchondo.sso.services.ReactiveUserService;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.CollectionUtils;

import reactor.core.publisher.Mono;

@ExtendWith({SpringExtension.class})
@WebFluxTest
@ContextConfiguration(classes = {
  LoginRoutes.class,
  LoginConfiguration.class,
  LoginHandler.class,
  Validate.class,
  GlobalErrorWebExceptionHandler.class
})
public class LoginHandlerTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private ReactiveUserService userService;

  @BeforeEach
  public void setup() {
    when(userService.saveUser(any(SaveUserDTO.class))).thenReturn(Mono.just(new UserDTO()));
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
      .value(response -> {
        assertNotNull(response);
      });
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
      .value(response -> {
        assertNotNull(response);
      });
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
      .value(response -> {
        assertNotNull(response);
      });
  }

  @Test
  public void testRegex() {
//    String responseString = mockMvc.perform(get("/regex"))
//        .andExpect(status().isOk())
//        .andReturn().getResponse().getContentAsString();
//
//    assertFalse(StringUtils.isEmpty(responseString));
//    Map<String, String> response = Mapper.readValue(responseString, new TypeReference<>(){});
//    assertNotNull(response);
//    assertEquals(3, response.size());
//    assertEquals(RegexConstants.USERNAME_REGEX, response.get("USERNAME_REGEX"));
//    assertEquals(RegexConstants.PASSWORD_REGEX, response.get("PASSWORD_REGEX"));
//    assertEquals(RegexConstants.EMAIL_REGEX, response.get("EMAIL_REGEX"));
  }
}
