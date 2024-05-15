package com.vanchondo.sso.configs;

import java.security.SecureRandom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    int strength = 10; // work factor of bcrypt
    return new BCryptPasswordEncoder(strength, new SecureRandom());
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
