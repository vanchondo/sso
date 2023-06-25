package com.vanchondo.sso.utilities;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import org.apache.commons.lang3.StringUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class Sanitize {

  public static String sanitize(String value) {
    if (StringUtils.isEmpty(value)){
      return value;
    }

    PolicyFactory policy = Sanitizers.FORMATTING;
    return policy.sanitize(value);
  }

  public static void sanitizeLoginDto(LoginDTO dto) {
    dto.setUsername(sanitize(dto.getUsername()));
    dto.setPassword(sanitize(dto.getPassword()));
  }

  public static void sanitizeSaveUserDTO(SaveUserDTO dto) {
    dto.setUsername(sanitize(dto.getUsername()));
    dto.setPassword(sanitize(dto.getPassword()));
    dto.setEmail(sanitize(dto.getEmail()));
  }

  public static void sanitizeCurrentUserDTO(CurrentUserDTO dto) {
    dto.setEmail(sanitize(dto.getEmail()));
    dto.setUsername(sanitize(dto.getUsername()));
    dto.setRole(sanitize(dto.getRole()));
    dto.setIss(sanitize(dto.getIss()));
  }

  public static void sanitizeUpdateUserDTO(UpdateUserDTO dto) {
    dto.setCurrentPassword(sanitize(dto.getCurrentPassword()));
    dto.setNewPassword(sanitize(dto.getNewPassword()));
  }

  public static void sanitizeDeleteUserDTO(DeleteUserDTO dto) {
    dto.setPassword(sanitize(dto.getPassword()));
  }
}
