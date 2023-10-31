package com.vanchondo.sso.services;

import com.vanchondo.sso.cacheServices.UserCacheService;
import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.BadRequestException;
import com.vanchondo.sso.exceptions.ConflictException;
import com.vanchondo.sso.exceptions.NotFoundException;
import com.vanchondo.sso.mappers.UserDTOMapper;
import com.vanchondo.sso.mappers.UserEntityMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@AllArgsConstructor
public class UserService {
  private final EmailService emailService;
  private final UserCacheService userCacheService;
  private final PasswordEncoder passwordEncoder;

  public Mono<UserDTO> saveUser(SaveUserDTO dto) {
    // Check if the email or username already exists
    Mono<Boolean> emailExists = userCacheService.existsByEmail(dto.getEmail());
    Mono<Boolean> usernameExists = userCacheService.existsByUsername(dto.getUsername());

    // Combine the two existence checks
    return Mono.zip(emailExists, usernameExists)
      .flatMap(tuple -> {
        boolean emailExistsValue = tuple.getT1();
        boolean usernameExistsValue = tuple.getT2();

        // If neither email nor username exists, proceed to save the user
        if (!emailExistsValue && !usernameExistsValue) {
          UserEntity entity = UserEntityMapper.map(dto);
          entity.setActive(false);
          entity.setLastUpdatedAt(LocalDateTime.now());
          entity.setPassword(passwordEncoder.encode(entity.getPassword()));
          entity.setVerificationToken(dto.isTest()
            ? dto.getCaptchaResponse() // Sets captcha response as token
            : UUID.randomUUID().toString() // Generates a random UUID
          );

          return userCacheService.save(entity)
            .flatMap(stored -> {
              if (!dto.isTest()) {
                // Send an email to the user and handle exceptions gracefully
                return emailService.sendEmailReactive(entity.getEmail(), entity.getVerificationToken())
                  .thenReturn(stored)
                  .onErrorResume(ex -> {
                    log.error("::saveUser:: Error sending email to={}", entity.getEmail(), ex);
                    return userCacheService.delete(entity)
                      .then(Mono.error(new ConflictException("Error sending email to=" + entity.getEmail())));
                  });
              } else {
                return Mono.just(stored);
              }
            })
            .map(UserDTOMapper::map);
        } else {
          // If email or username already exists, return a conflict error
          return Mono.error(new ConflictException("Email or username already registered"));
        }
      });
  }

  public Mono<Boolean> validateUser(ValidateUserDTO userDTO) {
    String email = userDTO.getEmail();
    String token = userDTO.getToken();
    String methodName="::validateUser::";
    log.info("{}Trying to validate email={} token={}", methodName, email, token);
    if (StringUtils.isEmpty(token) || StringUtils.isEmpty(email)) {
      log.error("{}Email and/or token are null", methodName);
      return Mono.error(new NotFoundException("User validation not found"));
    }
    return userCacheService.findByEmail(email)
      .flatMap(entity -> {
        if (StringUtils.isEmpty(entity.getUsername())) {
          log.error("{}User registry not found", methodName);
          return Mono.error(new NotFoundException("User validation not found"));
        }
        if (token.equals(entity.getVerificationToken())) {
          log.info("{}Activating user", methodName);
          entity.setActive(true);
          entity.setLastUpdatedAt(LocalDateTime.now());
          entity.setVerificationToken(null);
          return userCacheService.save(entity)
            .map(savedUser -> true);
        }
        else {
          log.error("{}Token not valid", methodName);
          return Mono.error(new BadRequestException("Token not valid"));
        }
      });

  }

  public Mono<UserEntity> findUserEntityByUsername(String username) {
    String methodName = "::callFindUserEntityByUserName::";
    log.info("{}Entering method", methodName);
    return userCacheService.findByUsername(username)
      .flatMap(entity -> {
        if (StringUtils.isEmpty(entity.getUsername())) {
          log.info("{}User not found", methodName);
          return Mono.error(new NotFoundException("User not found"));
        }

        return Mono.just(entity);
      });
  }

  public Mono<Boolean> deleteUser(DeleteUserDTO dto, CurrentUserDTO currentUser){
    String methodName = "::deleteUser::";
    return userCacheService.findByUsername(currentUser.getUsername())
      .flatMap(entity -> {
        if (StringUtils.isEmpty(entity.getUsername())) {
          log.warn("{}User not found for deletion. user={}", methodName, currentUser.getUsername());
          return Mono.error(new NotFoundException("User not found"));
        }
        else {
          if (passwordEncoder.matches(dto.getPassword(), entity.getPassword())){
            return userCacheService.delete(entity)
              .then(Mono.defer(() -> {
                log.info("{}User deleted successfully. user={}", methodName, currentUser.getUsername());
                return Mono.just(true);
              }));
          }
          else {
            log.warn("{}User cannot be deleted, password is not valid. user={}", methodName, currentUser.getUsername());
            return Mono.error(new ConflictException("Password is not valid"));
          }
        }
      });
  }

  public Mono<UserDTO> updateUser(UpdateUserDTO dto, CurrentUserDTO currentUser){
    String methodName = "::updateUser::";
    return userCacheService.findByUsername(currentUser.getUsername())
      .defaultIfEmpty(new UserEntity())
      .flatMap(entity -> {
        if (entity == null || StringUtils.isEmpty(entity.getUsername())) {
          log.warn("{}User not found to update. user={}", methodName, currentUser.getUsername());
          return Mono.error(new NotFoundException("User not found"));
        }
        else {
          if (passwordEncoder.matches(dto.getCurrentPassword(), entity.getPassword())){
            entity.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            entity.setLastUpdatedAt(LocalDateTime.now());

            return userCacheService.save(entity)
              .map(UserDTOMapper::map);
          }
          else {
            log.warn("{}Password is not valid. password={}", methodName, entity.getPassword());
            return Mono.error(new ConflictException("Password is not valid"));
          }
        }
      });
  }
}