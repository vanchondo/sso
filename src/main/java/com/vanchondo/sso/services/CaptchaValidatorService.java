package com.vanchondo.sso.services;

import com.vanchondo.sso.configs.properties.CaptchaConfiguration;
import com.vanchondo.sso.dtos.captcha.CaptchaResponseDTO;
import com.vanchondo.sso.dtos.security.CaptchaDTO;
import com.vanchondo.sso.exceptions.BadRequestException;
import com.vanchondo.sso.exceptions.ReCaptchaInvalidException;
import com.vanchondo.sso.utilities.NetworkUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class CaptchaValidatorService {
  private final CaptchaConfiguration configuration;
  private final RestTemplate restTemplate;
  private final LoadingCache<String, Integer> attemptsCache;

  public CaptchaValidatorService(CaptchaConfiguration configuration, RestTemplate restTemplate) {
    this.configuration = configuration;
    this.restTemplate = restTemplate;
    attemptsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(configuration.getMaxAttemptExpiration(), TimeUnit.HOURS)
        .build(new CacheLoader<>() {
          @Override
          public Integer load(String key) {
            return 0;
          }
        });
  }

  public void reCaptchaSucceeded(String key) {
    attemptsCache.invalidate(key);
  }

  public void reCaptchaFailed(String key) {
    int attempts = attemptsCache.getUnchecked(key);
    attempts++;
    attemptsCache.put(key, attempts);
  }

  public boolean isBlocked(String key) {
    return attemptsCache.getUnchecked(key) >= configuration.getMaxAttempt();
  }

  public boolean validateCaptcha(String captchaResponse, String clientIp) {
    if(isBlocked(clientIp)) {
      throw new BadRequestException("Client exceeded maximum number of failed attempts");
    }

    if (!validate(captchaResponse, clientIp)) {
      reCaptchaFailed(clientIp);
      throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
    }

    return true;
  }

  public Mono<Boolean> validateCaptcha(CaptchaDTO dto, ServerWebExchange exchange) {
    String methodName = "::validateCaptcha::";
    if (configuration.getSecret().equals(dto.getCaptchaResponse())){
      log.info("{}This request is for testing purposes, captcha secret provided", methodName);
      dto.setTest(true);
      return Mono.just(true);
    }
    else {
      return validateCaptchaReactive(dto.getCaptchaResponse(), NetworkUtil.getClientIp(exchange));
    }
  }

  private Mono<Boolean> validateCaptchaReactive(String captchaResponse, String clientIp) {
    if(isBlocked(clientIp)) {
      return Mono.error(new BadRequestException("Client exceeded maximum number of failed attempts"));
    }

    if (!validate(captchaResponse, clientIp)) {
      reCaptchaFailed(clientIp);
      return Mono.error(new ReCaptchaInvalidException("reCaptcha was not successfully validated"));
    }

    return Mono.just(true);
  }

  private boolean validate(String captchaResponse, String clientIp) {
    URI verifyUri = URI.create(
      String.format(configuration.getEndpoint(), configuration.getSecret(), captchaResponse, clientIp)
    );
    CaptchaResponseDTO response = restTemplate.getForObject(verifyUri, CaptchaResponseDTO.class);
    if(response == null || !response.isSuccess() || response.getScore() < configuration.getThreshold()) {
      reCaptchaFailed(clientIp);
      return false;
    }

    reCaptchaSucceeded(clientIp);
    return true;
  }
}
