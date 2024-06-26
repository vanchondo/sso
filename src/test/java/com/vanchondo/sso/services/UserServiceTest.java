package com.vanchondo.sso.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vanchondo.security.dto.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.BadRequestException;
import com.vanchondo.sso.exceptions.ConflictException;
import com.vanchondo.sso.exceptions.NotFoundException;
import com.vanchondo.sso.repositories.UserRepository;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.TestConstants;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith({SpringExtension.class})
public class UserServiceTest {

  @Mock private EmailService emailService;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private UserService userService;

  @BeforeEach
  public void setup() {
    when(passwordEncoder.encode(anyString())).thenReturn(TestConstants.PASSWORD);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(emailService.sendEmailReactive(anyString(), anyString())).thenReturn(Mono.empty());
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    UserEntity userEntity = ObjectFactory.createUserEntity();
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(userEntity));
    when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(userEntity));
    when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(userEntity));
    when(userRepository.delete(any(UserEntity.class))).thenReturn(Mono.empty());
  }

  @Test
  public void testSaveUserWhenSuccess() {
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
        .assertNext(
            result -> {
              verify(userRepository, times(1)).existsByEmail(anyString());
              verify(userRepository, times(1)).existsByUsername(anyString());
              verify(passwordEncoder, times(1)).encode(anyString());
              verify(userRepository, times(1)).save(any(UserEntity.class));
              verify(emailService, times(1)).sendEmailReactive(anyString(), anyString());
              verify(userRepository, times(0)).delete(any(UserEntity.class));
              assertNotNull(result);
            })
        .verifyComplete();
  }

  @Test
  public void testSaveUserWhenTestIsTrueAndGetSuccess() {
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(true);
    StepVerifier.create(userService.saveUser(saveUserDTO))
        .assertNext(
            result -> {
              verify(userRepository, times(1)).existsByEmail(anyString());
              verify(userRepository, times(1)).existsByUsername(anyString());
              verify(passwordEncoder, times(1)).encode(anyString());
              verify(userRepository, times(1)).save(any(UserEntity.class));
              verify(emailService, times(0)).sendEmailReactive(anyString(), anyString());
              verify(userRepository, times(0)).delete(any(UserEntity.class));
              assertNotNull(result);
            })
        .verifyComplete();
  }

  @Test
  public void testSaveUserWhenEmailAlreadyExists() {
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
        .expectError(ConflictException.class)
        .verify();
  }

  @Test
  public void testSaveUserWhenUsernameAlreadyExists() {
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
        .expectError(ConflictException.class)
        .verify();
  }

  @Test
  public void testSaveUserWhenSendEmailFails() {
    when(emailService.sendEmailReactive(anyString(), anyString()))
        .thenReturn(Mono.error(new MessagingException()));
    when(userRepository.delete(any(UserEntity.class))).thenReturn(Mono.empty());
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
        .expectError(ConflictException.class)
        .verify();
  }

  @Test
  public void testValidateUser() {
    ValidateUserDTO userDTO = ObjectFactory.createValidateUserDto();
    StepVerifier.create(userService.validateUser(userDTO))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
  }

  @Test
  public void testValidateUserWhenTokenIsEmpty() {
    ValidateUserDTO userDTO = ObjectFactory.createValidateUserDto();
    userDTO.setToken(null);
    StepVerifier.create(userService.validateUser(userDTO))
        .expectError(NotFoundException.class)
        .verify();
  }

  @Test
  public void testValidateUserWhenUserNotFound() {
    when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(new UserEntity()));
    ValidateUserDTO userDTO = ObjectFactory.createValidateUserDto();
    StepVerifier.create(userService.validateUser(userDTO))
        .expectError(NotFoundException.class)
        .verify();
  }

  @Test
  public void testValidateUserWhenTokenIsNotEquals() {
    ValidateUserDTO userDTO = ObjectFactory.createValidateUserDto();
    userDTO.setToken(TestConstants.TOKEN_SECRET_KEY + "12345");
    StepVerifier.create(userService.validateUser(userDTO))
        .expectError(BadRequestException.class)
        .verify();
  }

  @Test
  public void testFindUserEntityByUsernameWhenSuccess() {
    StepVerifier.create(userService.findUserEntityByUsername(TestConstants.USERNAME))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }

  @Test
  public void testFindUserEntityByUsernameWhenUserNotFound() {
    when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(new UserEntity()));
    StepVerifier.create(userService.findUserEntityByUsername(TestConstants.USERNAME))
        .expectError(NotFoundException.class)
        .verify();
  }

  @Test
  public void testDeleteUserWhenSuccess() {
    CurrentUserDTO currentUserDTO = ObjectFactory.createCurrentUserDto();
    DeleteUserDTO deleteUserDTO = ObjectFactory.createDeleteUserDto();
    StepVerifier.create(userService.deleteUser(deleteUserDTO, currentUserDTO))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
  }

  @Test
  public void testDeleteUserWhenUserNotFound() {
    when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(new UserEntity()));
    CurrentUserDTO currentUserDTO = ObjectFactory.createCurrentUserDto();
    DeleteUserDTO deleteUserDTO = ObjectFactory.createDeleteUserDto();
    StepVerifier.create(userService.deleteUser(deleteUserDTO, currentUserDTO))
        .expectError(NotFoundException.class)
        .verify();
  }

  @Test
  public void testDeleteUserWhenPasswordNotMatch() {
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    CurrentUserDTO currentUserDTO = ObjectFactory.createCurrentUserDto();
    DeleteUserDTO deleteUserDTO = ObjectFactory.createDeleteUserDto();
    StepVerifier.create(userService.deleteUser(deleteUserDTO, currentUserDTO))
        .expectError(ConflictException.class)
        .verify();
  }

  @Test
  public void testUpdateUserWhenSuccess() {
    CurrentUserDTO currentUserDTO = ObjectFactory.createCurrentUserDto();
    UpdateUserDTO updateUserDTO = ObjectFactory.createUpdateUserDto();

    StepVerifier.create(userService.updateUser(updateUserDTO, currentUserDTO))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }

  @Test
  public void testUpdateUserWhenUserNotFound() {
    when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());
    CurrentUserDTO currentUserDTO = ObjectFactory.createCurrentUserDto();
    UpdateUserDTO updateUserDTO = ObjectFactory.createUpdateUserDto();

    StepVerifier.create(userService.updateUser(updateUserDTO, currentUserDTO))
        .expectError(NotFoundException.class)
        .verify();
  }

  @Test
  public void testUpdateUserWhenPasswordNotMatch() {
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    CurrentUserDTO currentUserDTO = ObjectFactory.createCurrentUserDto();
    UpdateUserDTO updateUserDTO = ObjectFactory.createUpdateUserDto();

    StepVerifier.create(userService.updateUser(updateUserDTO, currentUserDTO))
        .expectError(ConflictException.class)
        .verify();
  }

  @Test
  public void testGetProfilePicture() {
    CurrentUserDTO currentUserDTO = ObjectFactory.createCurrentUserDto();
    StepVerifier.create(userService.getProfilePicture(currentUserDTO))
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
  }
}
