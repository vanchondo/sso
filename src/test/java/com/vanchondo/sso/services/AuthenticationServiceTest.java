package com.vanchondo.sso.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.utilities.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
public class AuthenticationServiceTest {
  @Mock
  private UserService userService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private LoginConfiguration loginConfiguration;
  @InjectMocks
  private AuthenticationService authenticationService;

  @Test
  public void testGetSigningKey() {
    assertNotNull(AuthenticationService.getSigningKey(TestConstants.TOKEN_SECRET_KEY));
  }


}
