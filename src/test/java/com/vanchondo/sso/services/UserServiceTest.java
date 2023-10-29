package com.vanchondo.sso.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.ConflictException;
import com.vanchondo.sso.repositories.ReactiveUserRepository;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.mail.MessagingException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith({SpringExtension.class})
public class UserServiceTest {

  @Mock
  private EmailService emailService;
  @Mock
  private ReactiveUserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @InjectMocks
  private UserService userService;

  @BeforeEach
  public void setup() {
    when(passwordEncoder.encode(anyString())).thenReturn(TestConstants.PASSWORD);
    when(emailService.sendEmailReactive(anyString(), anyString())).thenReturn(Mono.empty());
  }

  @Test
  public void testSaveUserWhenSuccess() {
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
      .assertNext(result -> {
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
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(true);
    StepVerifier.create(userService.saveUser(saveUserDTO))
      .assertNext(result -> {
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
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
      .expectError(ConflictException.class)
      .verify();
  }

  @Test
  public void testSaveUserWhenUsernameAlreadyExists() {
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
      .expectError(ConflictException.class)
      .verify();
  }

  @Test
  public void testSaveUserWhenSendEmailFails() {
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    when(emailService.sendEmailReactive(anyString(), anyString())).thenReturn(Mono.error(new MessagingException()));
    when(userRepository.delete(any(UserEntity.class))).thenReturn(Mono.empty());
    SaveUserDTO saveUserDTO = ObjectFactory.createSaveUserDTO();
    saveUserDTO.setTest(false);
    StepVerifier.create(userService.saveUser(saveUserDTO))
      .expectError(ConflictException.class)
      .verify();
  }
}
