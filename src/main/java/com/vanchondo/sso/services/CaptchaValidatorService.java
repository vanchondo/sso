package com.vanchondo.sso.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vanchondo.sso.configs.properties.CaptchaConfiguration;
import com.vanchondo.sso.dtos.captcha.ReCaptchaValidationResponse;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class CaptchaValidatorService {
  private final CaptchaConfiguration configuration;
  private final RestTemplate restTemplate;

  private LoadingCache<String, Integer> attemptsCache;

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

//  public boolean validateCaptcha(String captchaResponse, String clientIp) {
//    URI verifyUri = URI.create(
//        String.format(configuration.getEndpoint(), configuration.getSecretKey(), captchaResponse, clientIp)
//    );
//    CaptchaResponseDTO response = restTemplate.getForObject(verifyUri, CaptchaResponseDTO.class);
//    if(!response.isSuccess() || response.getScore() < configuration.getThreshold()) {
//      throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
//    }
//
//    return true;
//  }

  public boolean validateCaptcha(String response) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("secret", configuration.getSecretKey());
    body.add("response", response);

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<ReCaptchaValidationResponse> responseEntity = restTemplate.exchange(
        configuration.getEndpoint(),
        HttpMethod.POST,
        requestEntity,
        ReCaptchaValidationResponse.class
    );

    ReCaptchaValidationResponse responseBody = responseEntity.getBody();

    // Check if the reCAPTCHA response is valid
    return responseBody != null && responseBody.isSuccess();
  }
}
