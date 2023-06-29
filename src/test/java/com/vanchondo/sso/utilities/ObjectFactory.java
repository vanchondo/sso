package com.vanchondo.sso.utilities;

import com.vanchondo.sso.dtos.users.SaveUserDTO;

public abstract class ObjectFactory {
  private ObjectFactory() {}

  public static SaveUserDTO createSaveUserDTOWithInvalidProperties() {
    return new SaveUserDTO(
        "inv@lidUserN@ame$",
        "notAnEmail.com",
        "sPass" // short password
    );
  }

  public static SaveUserDTO createSaveUserDTO() {
    return new SaveUserDTO(
        "validUsername",
        "victor@email.com",
        "myPassword"
    );
  }

}
