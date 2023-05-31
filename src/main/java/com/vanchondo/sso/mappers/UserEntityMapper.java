package com.vanchondo.sso.mappers;

import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.entities.UserEntity;

public class UserEntityMapper {

    public static UserEntity map(SaveUserDTO dto){
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());

        return entity;
    }

}
