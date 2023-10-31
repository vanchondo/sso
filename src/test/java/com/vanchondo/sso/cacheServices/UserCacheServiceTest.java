package com.vanchondo.sso.cacheServices;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.repositories.ReactiveUserRepository;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith({SpringExtension.class})
public class UserCacheServiceTest {
  @Mock
  private ReactiveUserRepository userRepository;
  @Mock
  private CacheManager cacheManager;
  @Mock
  private Cache cache;
  private UserCacheService userCacheService;

  @BeforeEach
  public void setup() {
    when(cacheManager.getCache(anyString())).thenReturn(cache);
    when(cache.get(anyString(), eq(UserEntity.class))).thenReturn(null);
    doNothing().when(cache).put(anyString(), eq(UserEntity.class));
    when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    when(userRepository.delete(any(UserEntity.class))).thenReturn(Mono.empty());
    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(ObjectFactory.createUserEntity()));
    when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));

    userCacheService = new UserCacheService(userRepository, cacheManager);
  }

  @Test
  public void testFindByUsernameWhenCacheManagerIsNull() {
    cacheManager = null;
    userCacheService = new UserCacheService(userRepository, cacheManager);

    StepVerifier.create(userCacheService.findByUsername(TestConstants.USERNAME))
      .assertNext(Assertions::assertNotNull)
      .verifyComplete();
  }

  @Test
  public void testFindByUsernameWhenUserEntityIsNotCached() {
    StepVerifier.create(userCacheService.findByUsername(TestConstants.USERNAME))
      .assertNext(Assertions::assertNotNull)
      .verifyComplete();
  }

  @Test
  public void testFindByUsernameWhenUserEntityIsCached() {
    when(cache.get(anyString(), eq(UserEntity.class))).thenReturn(ObjectFactory.createUserEntity());
    StepVerifier.create(userCacheService.findByUsername(TestConstants.USERNAME))
      .assertNext(Assertions::assertNotNull)
      .verifyComplete();
  }

  @Test
  public void testFindByUsernameWhenUserEntityCacheThrowsException() {
    when(cache.get(anyString(), eq(UserEntity.class))).thenThrow(new IllegalStateException());
    StepVerifier.create(userCacheService.findByUsername(TestConstants.USERNAME))
      .assertNext(Assertions::assertNotNull)
      .verifyComplete();
  }

  @Test
  public void testDeleteWhenSuccess() {
    StepVerifier.create(userCacheService.delete(ObjectFactory.createUserEntity()))
      .assertNext(Assertions::assertTrue)
      .verifyComplete();
  }

  @Test
  public void testSave() {
    StepVerifier.create(userCacheService.save(ObjectFactory.createUserEntity()))
      .assertNext(Assertions::assertNotNull)
      .verifyComplete();
  }

  @Test
  public void testExistsByEmail() {
    StepVerifier.create(userCacheService.existsByEmail(TestConstants.EMAIL))
      .assertNext(Assertions::assertTrue)
      .verifyComplete();
  }

  @Test
  public void testExistsByUsername() {
    StepVerifier.create(userCacheService.existsByUsername(TestConstants.USERNAME))
      .assertNext(Assertions::assertTrue)
      .verifyComplete();
  }

  @Test
  public void testFindByEmail() {
    StepVerifier.create(userCacheService.findByEmail(TestConstants.EMAIL))
      .assertNext(Assertions::assertNotNull)
      .verifyComplete();
  }

}
