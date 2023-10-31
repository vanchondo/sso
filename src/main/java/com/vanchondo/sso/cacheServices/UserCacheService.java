package com.vanchondo.sso.cacheServices;

import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.repositories.ReactiveUserRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class UserCacheService extends CacheService {

  private final ReactiveUserRepository userRepository;

  private static final String SERVICE_NAME = "UserCache-";
  private static final String FIND_BY_USERNAME_CACHE = SERVICE_NAME + "findByUsername";

  public UserCacheService(ReactiveUserRepository userRepository, CacheManager cacheManager) {
    super(cacheManager);
    this.userRepository = userRepository;
  }

  public Mono<UserEntity> findByUsername(String username) {
    String methodName = String.format("::%s::", new Object() {}.getClass().getEnclosingMethod().getName());
    Cache cache = getServiceCache(FIND_BY_USERNAME_CACHE);
    if (cache != null) {
      try {
        UserEntity userEntity = cache.get(username, UserEntity.class);
        if (userEntity != null) {
          log.info("{}Returning entity from cache username={}", methodName, username);
          return Mono.just(userEntity);
        }
      } catch (IllegalStateException ex) {
        log.error("{}Failed to read cache", methodName, ex);
      }
      return callFindUserEntityByUserName(username)
        .map(userEntity -> {
          cache.put(username, userEntity);
          return userEntity;
        });
    }

    return callFindUserEntityByUserName(username);
  }

  public Mono<Boolean> delete(UserEntity entity) {
    String methodName = String.format("::%s::", new Object() {}.getClass().getEnclosingMethod().getName());

    return userRepository.delete(entity)
      .then(Mono.defer(() -> {
        log.info("{}User deleted successfully. user={}", methodName, entity.getUsername());
        Cache cache = getServiceCache(FIND_BY_USERNAME_CACHE);
        if (cache != null) {
          cache.evictIfPresent(entity.getUsername());
          log.info("{}Cache evicted for username={}", methodName, entity.getUsername());
        }
        return Mono.just(true);
      }));
  }

  public Mono<UserEntity> save(UserEntity entity) {
    return userRepository.save(entity)
      .map(stored -> {
        Cache cache = getServiceCache(FIND_BY_USERNAME_CACHE);
        if (cache != null) {
          cache.put(entity.getUsername(), stored);
        }
        return stored;
      });
  }

  public Mono<Boolean> existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public Mono<Boolean> existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  public Mono<UserEntity> findByEmail(String email) {
    return userRepository.findByEmail(email)
      .defaultIfEmpty(new UserEntity());
  }

  private Mono<UserEntity> callFindUserEntityByUserName(String username) {
    String methodName = String.format("::%s::", new Object() {}.getClass().getEnclosingMethod().getName());
    log.info("{}Entering method", methodName);
    return userRepository.findByUsername(username)
      .defaultIfEmpty(new UserEntity());
  }

}
