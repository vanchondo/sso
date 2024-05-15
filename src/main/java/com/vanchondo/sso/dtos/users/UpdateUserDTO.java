package com.vanchondo.sso.dtos.users;

import com.vanchondo.sso.utilities.RegexConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserDTO {

  @NotNull(message = "CurrentPassword is required")
  @Pattern(regexp = RegexConstants.PASSWORD_REGEX, message = "Password not valid, min=6, max=50")
  private String currentPassword;

  @NotNull(message = "NewPassword is required")
  @Pattern(regexp = RegexConstants.PASSWORD_REGEX, message = "Password not valid, min=6, max=50")
  private String newPassword;
}
