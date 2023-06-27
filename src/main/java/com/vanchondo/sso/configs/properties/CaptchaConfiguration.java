package com.vanchondo.sso.configs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.vanchondo.recaptcha")
@Getter
@Setter
public class CaptchaConfiguration {
  private String endpoint;
  private String secretKey;
  private double threshold;
  private int maxAttempt;
  private int maxAttemptExpiration;
}
