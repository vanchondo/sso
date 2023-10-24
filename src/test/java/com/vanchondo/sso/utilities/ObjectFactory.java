package com.vanchondo.sso.utilities;

import static com.vanchondo.sso.services.AuthenticationService.getSigningKey;

import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;

import java.util.Calendar;
import java.util.Date;

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

}
