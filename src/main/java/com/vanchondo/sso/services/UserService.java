package com.vanchondo.sso.services;

import com.vanchondo.security.dto.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.entities.PictureEntity;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.BadRequestException;
import com.vanchondo.sso.exceptions.ConflictException;
import com.vanchondo.sso.exceptions.NotFoundException;
import com.vanchondo.sso.mappers.UserDTOMapper;
import com.vanchondo.sso.mappers.UserEntityMapper;
import com.vanchondo.sso.repositories.UserRepository;
import com.vanchondo.sso.utilities.LogUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@AllArgsConstructor
public class UserService {
  private final EmailService emailService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Saves a new user in the database, generating a verification token and sending an email to the
   * user.
   *
   * @param dto the user data transfer object
   * @return a Mono containing the saved user entity
   * @throws ConflictException if the email or username already exists
   */
  public Mono<UserDTO> saveUser(SaveUserDTO dto) {
    String methodName = LogUtil.getMethodName(new Object() {});
    log.info("{}Entering method", methodName);
    // Check if the email or username already exists
    Mono<Boolean> emailExists = userRepository.existsByEmail(dto.getEmail());
    Mono<Boolean> usernameExists = userRepository.existsByUsername(dto.getUsername());

    // Combine the two existence checks
    return Mono.zip(emailExists, usernameExists)
        .flatMap(
            tuple -> {
              boolean emailExistsValue = tuple.getT1();
              boolean usernameExistsValue = tuple.getT2();

              // If neither email nor username exists, proceed to save the user
              if (!emailExistsValue && !usernameExistsValue) {
                UserEntity entity = UserEntityMapper.map(dto);
                entity.setActive(false);
                entity.setLastUpdatedAt(LocalDateTime.now());
                entity.setPassword(passwordEncoder.encode(entity.getPassword()));
                entity.setVerificationToken(
                    dto.isTest()
                        ? dto.getCaptchaResponse() // Sets captcha response as token
                        : UUID.randomUUID().toString() // Generates a random UUID
                    );

                return userRepository
                    .save(entity)
                    .flatMap(
                        stored -> {
                          if (!dto.isTest()) {
                            // Send an email to the user and handle exceptions gracefully
                            return emailService
                                .sendEmailReactive(entity.getEmail(), entity.getVerificationToken())
                                .thenReturn(stored)
                                .onErrorResume(
                                    ex -> {
                                      log.error(
                                          "{} Error sending email to={}",
                                          methodName,
                                          entity.getEmail(),
                                          ex);
                                      return userRepository
                                          .delete(entity)
                                          .then(
                                              Mono.error(
                                                  new ConflictException(
                                                      "Error sending email to="
                                                          + entity.getEmail())));
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

  /**
   * Validates a user by checking if their email and token are valid.
   *
   * @param userDTO the {@link ValidateUserDTO} containing the user's email and token
   * @return a Mono emitting a Boolean value indicating whether the user was validated successfully
   *     or not
   */
  public Mono<Boolean> validateUser(ValidateUserDTO userDTO) {
    String methodName = LogUtil.getMethodName(new Object() {});
    log.info("{}Entering method", methodName);
    String email = userDTO.getEmail();
    String token = userDTO.getToken();
    log.info("{}Trying to validate email={} token={}", methodName, email, token);
    if (StringUtils.isEmpty(token) || StringUtils.isEmpty(email)) {
      log.error("{}Email and/or token are null", methodName);
      return Mono.error(new NotFoundException("User validation not found"));
    }
    return userRepository
        .findByEmail(email)
        .defaultIfEmpty(new UserEntity())
        .flatMap(
            entity -> {
              if (StringUtils.isEmpty(entity.getUsername())) {
                log.error("{}User registry not found", methodName);
                return Mono.error(new NotFoundException("User validation not found"));
              }
              if (token.equals(entity.getVerificationToken())) {
                log.info("{}Activating user", methodName);
                entity.setActive(true);
                entity.setLastUpdatedAt(LocalDateTime.now());
                entity.setVerificationToken(null);
                return userRepository.save(entity).map(savedUser -> true);
              } else {
                log.error("{}Token not valid", methodName);
                return Mono.error(new BadRequestException("Token not valid"));
              }
            });
  }

  /**
   * Find a UserEntity by username in the database.
   *
   * @param username The username of the user to find
   * @return A Mono<UserEntity> object representing the found user, or null if no user was found.
   */
  public Mono<UserEntity> findUserEntityByUsername(String username) {
    String methodName = LogUtil.getMethodName(new Object() {});
    log.info("{}Entering method", methodName);
    return userRepository
        .findByUsername(username)
        .defaultIfEmpty(new UserEntity())
        .flatMap(
            entity -> {
              if (StringUtils.isEmpty(entity.getUsername())) {
                log.info("{}User not found", methodName);
                return Mono.error(new NotFoundException("User not found"));
              }

              return Mono.just(entity);
            });
  }

  /**
   * Deletes a user based on the current user and their password.
   *
   * @param dto the delete user DTO containing the password to be verified
   * @param currentUserDTO the current user information
   * @return a mono that represents the result of deleting the user, true if successful, false
   *     otherwise
   */
  public Mono<Boolean> deleteUser(DeleteUserDTO dto, CurrentUserDTO currentUser) {
    String methodName = LogUtil.getMethodName(new Object() {});
    log.info("{}Entering method", methodName);
    return userRepository
        .findByUsername(currentUser.getUsername())
        .defaultIfEmpty(new UserEntity())
        .flatMap(
            entity -> {
              if (StringUtils.isEmpty(entity.getUsername())) {
                log.warn(
                    "{}User not found for deletion. user={}",
                    methodName,
                    currentUser.getUsername());
                return Mono.error(new NotFoundException("User not found"));
              } else {
                if (passwordEncoder.matches(dto.getPassword(), entity.getPassword())) {
                  return userRepository
                      .delete(entity)
                      .then(
                          Mono.defer(
                              () -> {
                                log.info(
                                    "{}User deleted successfully. user={}",
                                    methodName,
                                    currentUser.getUsername());
                                return Mono.just(true);
                              }));
                } else {
                  log.warn(
                      "{}User cannot be deleted, password is not valid. user={}",
                      methodName,
                      currentUser.getUsername());
                  return Mono.error(new ConflictException("Password is not valid"));
                }
              }
            });
  }

  /**
   * Updates a UserDTO based on an UpdateUserDTO and returns it as a Mono.
   *
   * @param dto The update user DTO.
   * @param currentUser The CurrentUserDTO.
   * @return A mono of the updated user.
   */
  public Mono<UserDTO> updateUser(UpdateUserDTO dto, CurrentUserDTO currentUser) {
    String methodName = LogUtil.getMethodName(new Object() {});
    log.info("{}Entering method", methodName);
    return findUserEntityByUsername(currentUser.getUsername())
        .defaultIfEmpty(new UserEntity())
        .flatMap(
            entity -> {
              if (passwordEncoder.matches(dto.getCurrentPassword(), entity.getPassword())) {
                entity.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                entity.setLastUpdatedAt(LocalDateTime.now());

                return userRepository.save(entity).map(UserDTOMapper::map);
              } else {
                log.warn("{}Password is not valid. password={}", methodName, entity.getPassword());
                return Mono.error(new ConflictException("Password is not valid"));
              }
            });
  }

  /**
   * Get the current user's profile picture.
   *
   * @param currentUser The current user DTO
   * @return The profile picture entity, or a new empty entity if not found
   */
  public Mono<PictureEntity> getProfilePicture(CurrentUserDTO currentUser) {
    String methodName = LogUtil.getMethodName(new Object() {});
    log.info("{}Entering method", methodName);
    return findUserEntityByUsername(currentUser.getUsername())
        .map(
            entity ->
                Optional.ofNullable(entity)
                    .map(UserEntity::getProfilePicture)
                    .orElse(new PictureEntity()))
        .defaultIfEmpty(new PictureEntity());
  }
}
