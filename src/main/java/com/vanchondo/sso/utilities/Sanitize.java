package com.vanchondo.sso.utilities;

import com.vanchondo.sso.dtos.users.DeleteUserDTO;
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



  public static void sanitizeDeleteUserDTO(DeleteUserDTO dto) {
    dto.setPassword(sanitize(dto.getPassword()));
  }
}
