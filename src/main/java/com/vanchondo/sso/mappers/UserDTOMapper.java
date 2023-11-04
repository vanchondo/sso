package com.vanchondo.sso.mappers;

import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.entities.PictureEntity;
import com.vanchondo.sso.entities.UserEntity;

import java.util.Optional;

public class UserDTOMapper {

    public static UserDTO map(UserEntity entity){
        return new UserDTO(
          entity.getUsername(),
          entity.getEmail(),
          Optional.ofNullable(entity.getProfilePicture())
            .map(PictureEntity::getPicture)
            .orElse(null)
        );
    }
}
