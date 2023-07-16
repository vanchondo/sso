package com.vanchondo.sso.utilities;

import com.vanchondo.sso.dtos.users.SaveUserDTO;

public abstract class ObjectFactory {
  private ObjectFactory() {}

  public static SaveUserDTO createSaveUserDTOWithInvalidProperties() {
    SaveUserDTO dto = new SaveUserDTO(
        "inv@lidUserN@ame$",
        "notAnEmail.com",
        "sPass" // short password
    );
    dto.setCaptchaResponse("captchaResponse");

    return dto;
  }

  public static SaveUserDTO createSaveUserDTO() {
    SaveUserDTO dto = new SaveUserDTO(
        "validUsername",
        "victor@email.com",
        "myPassword"
    );
    dto.setCaptchaResponse("captchaResponse");

    return dto;
  }

}
