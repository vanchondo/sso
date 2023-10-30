package com.vanchondo.sso.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vanchondo.sso.configs.properties.CaptchaConfiguration;
import com.vanchondo.sso.dtos.security.CaptchaDTO;
import com.vanchondo.sso.exceptions.BadRequestException;
import com.vanchondo.sso.exceptions.ReCaptchaInvalidException;
import com.vanchondo.sso.utilities.ObjectFactory;
import com.vanchondo.sso.utilities.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

import reactor.test.StepVerifier;

@ExtendWith({SpringExtension.class})
public class CaptchaValidatorServiceTest {
  @Mock
  private RestTemplate restTemplate;
  @Mock
  private ServerWebExchange exchange;
  private CaptchaValidatorService captchaValidatorService;
  private static int MAX_ATTEMPTS = 4;
  private static double THRESHOLD = .5;

  @BeforeEach
  public void setup() {
    CaptchaConfiguration configuration = new CaptchaConfiguration();
    configuration.setEndpoint("https://captchaEndpoint.com");
    configuration.setSecret(TestConstants.TOKEN_SECRET_KEY);
    configuration.setThreshold(THRESHOLD);
    configuration.setMaxAttempt(MAX_ATTEMPTS);
    configuration.setMaxAttemptExpiration(MAX_ATTEMPTS);
    captchaValidatorService = new CaptchaValidatorService(configuration, restTemplate);

    String ip = "192.168.1.1";
    InetAddress address = mock(InetAddress.class);
    when(address.getHostAddress()).thenReturn(ip);
    InetSocketAddress socket = mock(InetSocketAddress.class);
    when(socket.getAddress()).thenReturn(address);
    ServerHttpRequest request = mock(ServerHttpRequest.class);
    when(request.getRemoteAddress()).thenReturn(socket);
    when(exchange.getRequest()).thenReturn(request);

    when(restTemplate.getForObject(any(URI.class), any())).thenReturn(ObjectFactory.createCaptchaResponseDTO(true, THRESHOLD + .1d));
  }

  @Test
  public void testValidateCaptchaWhenCaptchaIsForTesting() {
    CaptchaDTO captchaDTO = ObjectFactory.createCaptchaDto();
    captchaDTO.setCaptchaResponse(TestConstants.TOKEN_SECRET_KEY);
    StepVerifier.create(captchaValidatorService.validateCaptcha(captchaDTO, exchange))
      .assertNext(result -> {
        assertTrue(result);
        assertTrue(captchaDTO.isTest());
      })
      .verifyComplete();
  }

  @Test
  public void testValidateCaptchaWhenSuccess() {
    CaptchaDTO captchaDTO = ObjectFactory.createCaptchaDto();
    StepVerifier.create(captchaValidatorService.validateCaptcha(captchaDTO, exchange))
      .assertNext(result -> {
        assertTrue(result);
        assertFalse(captchaDTO.isTest());
      })
      .verifyComplete();
  }

  @Test
  public void testValidateCaptchaWhenSuccessIsFalse() {
    when(restTemplate.getForObject(any(URI.class), any())).thenReturn(ObjectFactory.createCaptchaResponseDTO(false, THRESHOLD + .1d));
    CaptchaDTO captchaDTO = ObjectFactory.createCaptchaDto();
    StepVerifier.create(captchaValidatorService.validateCaptcha(captchaDTO, exchange))
      .expectError(ReCaptchaInvalidException.class)
      .verify();
  }

  @Test
  public void testValidateCaptchaWhenScoreIsLow() {
    when(restTemplate.getForObject(any(URI.class), any())).thenReturn(ObjectFactory.createCaptchaResponseDTO(true, THRESHOLD - .1d));
    CaptchaDTO captchaDTO = ObjectFactory.createCaptchaDto();
    StepVerifier.create(captchaValidatorService.validateCaptcha(captchaDTO, exchange))
      .expectError(ReCaptchaInvalidException.class)
      .verify();
  }

  @Test
  public void testValidateCaptchaWhenMaxAttemptsHasReached() {
    for (int i=0; i<MAX_ATTEMPTS; i++){
      testValidateCaptchaWhenSuccessIsFalse();
    }
    CaptchaDTO captchaDTO = ObjectFactory.createCaptchaDto();
    StepVerifier.create(captchaValidatorService.validateCaptcha(captchaDTO, exchange))
      .expectError(BadRequestException.class)
      .verify();
  }
}
