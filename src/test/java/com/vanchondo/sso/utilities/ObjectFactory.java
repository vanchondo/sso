package com.vanchondo.sso.utilities;

import static com.vanchondo.sso.services.AuthenticationService.getSigningKey;

import com.vanchondo.sso.configs.properties.UrlResource;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.entities.UserEntity;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public abstract class ObjectFactory {
  private ObjectFactory() {}

  public static SaveUserDTO createSaveUserDTOWithInvalidProperties() {
    SaveUserDTO dto = new SaveUserDTO(
        "inv@lidUserN@ame$",
        "notAnEmail.com",
        "sPass" // short password
    );
    dto.setCaptchaResponse("captchaResponse");

    return dto;
  }

  public static SaveUserDTO createSaveUserDTO() {
    SaveUserDTO dto = new SaveUserDTO(
        TestConstants.USERNAME,
        TestConstants.EMAIL,
        "myPassword"
    );
    dto.setCaptchaResponse("captchaResponse");

    return dto;
  }

  public static TokenDTO createTokenDTO() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.MINUTE, TestConstants.TOKEN_EXPIRATION);

    return new TokenDTO(Jwts.builder()
      .setIssuer(TestConstants.TOKEN_ISSUER)
      .setSubject(TestConstants.EMAIL)
      .claim(Constants.CLAIM_USERNAME_PROPERTY, TestConstants.USERNAME)
      .setIssuedAt(new Date())
      .setExpiration(cal.getTime())
      .signWith(
        getSigningKey(TestConstants.TOKEN_SECRET_KEY),
        SignatureAlgorithm.HS256
      )
      .compact());
  }

  public static List<UrlResource> createUnsecureUrls() {
    return Arrays.asList(
      new UrlResource("/swagger*", Collections.singletonList(HttpMethod.GET.name())),
      new UrlResource("/v2/api-docs", Collections.singletonList(HttpMethod.GET.name())),
      new UrlResource("/login", Collections.singletonList(HttpMethod.POST.name())),
      new UrlResource("/validate", Collections.singletonList(HttpMethod.POST.name())),
      new UrlResource("/register", Collections.singletonList(HttpMethod.POST.name())),
      new UrlResource("/regex", Collections.singletonList(HttpMethod.GET.name())),
      new UrlResource("/users/available", Collections.singletonList(HttpMethod.GET.name()))
    );
  }

  public static UserEntity createUserEntity() {
    UserEntity user = new UserEntity();
    user.setActive(true);
    user.setPassword(TestConstants.PASSWORD);
    user.setEmail(TestConstants.EMAIL);
    user.setUsername(TestConstants.USERNAME);

    return user;
  }

  public static LoginDTO createLoginDto() {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setUsername(TestConstants.USERNAME);
    loginDTO.setPassword(TestConstants.PASSWORD);

    return loginDTO;
  }

}
