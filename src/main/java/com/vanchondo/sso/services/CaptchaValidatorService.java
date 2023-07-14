package com.vanchondo.sso.services;

import com.vanchondo.sso.configs.properties.CaptchaConfiguration;
import com.vanchondo.sso.dtos.captcha.CaptchaResponseDTO;
import com.vanchondo.sso.exceptions.BadRequestException;
import com.vanchondo.sso.exceptions.ReCaptchaInvalidException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.log4j.Log4j2;

import java.net.URI;
import java.util.concurrent.TimeUnit;

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

    URI verifyUri = URI.create(
        String.format(configuration.getEndpoint(), configuration.getSecret(), captchaResponse, clientIp)
    );
    CaptchaResponseDTO response = restTemplate.getForObject(verifyUri, CaptchaResponseDTO.class);
    if(response == null || !response.isSuccess() || response.getScore() < configuration.getThreshold()) {
      reCaptchaFailed(clientIp);
      throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
    }

    reCaptchaSucceeded(clientIp);
    return true;
  }
}
