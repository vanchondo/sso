package com.vanchondo.sso.configs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "com.vanchondo.login")
@Getter
@Setter
public class LoginConfiguration {
    private String secretKey;
    private String issuer;
    private int expirationToken;
    private List<UrlResource> unsecuredUrls;
}