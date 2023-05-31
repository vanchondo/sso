package com.vanchondo.sso.repositories;

import com.vanchondo.sso.entities.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserEntity, String> {

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
}
