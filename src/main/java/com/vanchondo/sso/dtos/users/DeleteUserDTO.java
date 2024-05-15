package com.vanchondo.sso.dtos.users;

import com.vanchondo.sso.dtos.security.CaptchaDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeleteUserDTO extends CaptchaDTO {

  @NotNull(message = "Password is required")
  @Size(min = 6, max = 50, message = "Password not valid, min=6, max=50")
  private String password;
}
