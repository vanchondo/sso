package com.vanchondo.sso.dtos.security;

import com.vanchondo.sso.utilities.RegexConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginDTO extends CaptchaDTO {
  @NotNull(message = "Username is required")
  @Pattern(regexp = RegexConstants.USERNAME_REGEX, message = "Username not valid min=6, max=29")
  private String username;

  @NotNull(message = "Password is required")
  @Pattern(regexp = RegexConstants.PASSWORD_REGEX, message = "Password not valid, min=6, max=50")
  private String password;
}
