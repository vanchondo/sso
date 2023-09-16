package com.vanchondo.sso.repositories;

import com.vanchondo.sso.entities.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveMongoRepository<UserEntity, String> {

    Mono<UserEntity> findByUsername(String username);
    Mono<UserEntity> findByEmail(String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
}
