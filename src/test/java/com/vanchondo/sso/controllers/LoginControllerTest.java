package com.vanchondo.sso.controllers;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.dtos.ErrorDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.repositories.UserRepository;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.services.UserService;
import com.vanchondo.sso.utilities.Mapper;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.RegexConstants;

import com.fasterxml.jackson.core.type.TypeReference;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.CollectionUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(LoginConfiguration.class)
public class LoginControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private UserService userService;
  @MockBean
  private AuthenticationService authenticationService;
  @MockBean
  private UserRepository userRepository;

  @BeforeEach
  public void setup() {
    when(userService.saveUser(any(SaveUserDTO.class))).thenReturn(new UserDTO());
  }

  @Test
  public void testRegisterWhenRequiredParametersAreNotIncluded() throws Exception {
    String responseString = mockMvc.perform(post("/register")
            .content(Mapper.toJson(new SaveUserDTO()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andReturn().getResponse().getContentAsString();

    ErrorDTO response = Mapper.readValue(responseString, new TypeReference<>(){});
    assertNotNull(response);
    assertFalse(CollectionUtils.isEmpty(response.getMessages()));
    assertEquals(4, response.getMessages().size());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getError());
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    assertTrue(response.getMessages().contains("Username is required"));
    assertTrue(response.getMessages().contains("Email is required"));
    assertTrue(response.getMessages().contains("Password is required"));
  }

  @Test
  public void testRegisterWhenParametersAreNotValid() throws Exception {
    SaveUserDTO invalidDto = ObjectFactory.createSaveUserDTOWithInvalidProperties();
    String responseString = mockMvc.perform(post("/register")
            .content(Mapper.toJson(invalidDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andReturn().getResponse().getContentAsString();

    ErrorDTO response = Mapper.readValue(responseString, new TypeReference<>(){});
    assertNotNull(response);
    assertFalse(CollectionUtils.isEmpty(response.getMessages()));
    assertEquals(3, response.getMessages().size());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getError());
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    assertTrue(response.getMessages().contains("Username not valid min=6, max=29"));
    assertTrue(response.getMessages().contains("Email not valid"));
    assertTrue(response.getMessages().contains("Password not valid, min=6, max=50"));
  }

  @Test
  public void testRegisterWhenSuccess() throws Exception {
    SaveUserDTO dto = ObjectFactory.createSaveUserDTO();
    String responseString = mockMvc.perform(post("/register")
            .content(Mapper.toJson(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    UserDTO response = Mapper.readValue(responseString, new TypeReference<>(){});
    assertNotNull(response);
  }

  @Test
  public void testRegex() throws Exception {
    String responseString = mockMvc.perform(get("/regex"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    assertFalse(StringUtils.isEmpty(responseString));
    Map<String, String> response = Mapper.readValue(responseString, new TypeReference<>(){});
    assertNotNull(response);
    assertEquals(3, response.size());
    assertEquals(RegexConstants.USERNAME_REGEX, response.get("USERNAME_REGEX"));
    assertEquals(RegexConstants.PASSWORD_REGEX, response.get("PASSWORD_REGEX"));
    assertEquals(RegexConstants.EMAIL_REGEX, response.get("EMAIL_REGEX"));
  }

}