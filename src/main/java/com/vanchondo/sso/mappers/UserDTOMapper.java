package com.vanchondo.sso.mappers;

import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.entities.UserEntity;

public class UserDTOMapper {

  public static UserDTO map(UserEntity entity) {
    return new UserDTO(entity.getUsername(), entity.getEmail());
  }
}
