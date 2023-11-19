package com.vanchondo.sso.utilities;


import com.vanchondo.security.configs.properties.UrlResource;
import com.vanchondo.security.dto.CurrentUserDTO;
import com.vanchondo.sso.dtos.captcha.CaptchaResponseDTO;
import com.vanchondo.sso.dtos.security.CaptchaDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.entities.PictureEntity;
import com.vanchondo.sso.entities.UserEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class ObjectFactory {
  private ObjectFactory() {}

  public static SaveUserDTO createSaveUserDTOWithInvalidProperties() {
    SaveUserDTO dto = new SaveUserDTO(

      "inv@lidUserN@ame$",
      "notAnEmail.com",
      "sPass", // short password
      TestConstants.PICTURE_BASE64
    );
    dto.setCaptchaResponse("captchaResponse");

    return dto;
  }

  public static SaveUserDTO createSaveUserDTO() {
    SaveUserDTO dto = new SaveUserDTO(
      TestConstants.USERNAME,
      TestConstants.EMAIL,
      "myPassword",
      TestConstants.PICTURE_BASE64
    );
    dto.setCaptchaResponse("captchaResponse");

    return dto;
  }

//  public static TokenDTO createTokenDTO() {
//    Calendar cal = Calendar.getInstance();
//    cal.setTime(new Date());
//    cal.add(Calendar.MINUTE, TestConstants.TOKEN_EXPIRATION);
//
//    return new TokenDTO(Jwts.builder()
//      .setIssuer(TestConstants.TOKEN_ISSUER)
//      .setSubject(TestConstants.EMAIL)
//      .claim(Constants.CLAIM_USERNAME_PROPERTY, TestConstants.USERNAME)
//      .setIssuedAt(new Date())
//      .setExpiration(cal.getTime())
//      .signWith(
//        getSigningKey(TestConstants.TOKEN_SECRET_KEY),
//        SignatureAlgorithm.HS256
//      )
//      .compact());
//  }

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
    user.setVerificationToken(TestConstants.TOKEN_SECRET_KEY);
    user.setProfilePicture(createPictureEntity());

    return user;
  }

  public static PictureEntity createPictureEntity() {
    return createPictureEntity(TestConstants.PICTURE_BASE64.getBytes(StandardCharsets.UTF_8));
  }

  public static PictureEntity createPictureEntity(byte[] content) {
    return new PictureEntity(
      MediaType.IMAGE_JPEG_VALUE,
      content
    );
  }

  public static LoginDTO createLoginDto() {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setUsername(TestConstants.USERNAME);
    loginDTO.setPassword(TestConstants.PASSWORD);
    loginDTO.setCaptchaResponse(TestConstants.TOKEN_SECRET_KEY);

    return loginDTO;
  }

  public static ValidateUserDTO createValidateUserDto() {
    ValidateUserDTO dto = new ValidateUserDTO();
    dto.setEmail(TestConstants.EMAIL);
    dto.setToken(TestConstants.TOKEN_SECRET_KEY);

    return dto;
  }

  public static DeleteUserDTO createDeleteUserDto() {
    DeleteUserDTO dto = new DeleteUserDTO();
    dto.setPassword(TestConstants.PASSWORD);
    dto.setCaptchaResponse(TestConstants.TOKEN_SECRET_KEY);

    return dto;
  }

  public static CurrentUserDTO createCurrentUserDto() {
    CurrentUserDTO dto = new CurrentUserDTO();
    dto.setEmail(TestConstants.EMAIL);
    dto.setUsername(TestConstants.USERNAME);
    dto.setExp(new Date());

    return dto;
  }

  public static UpdateUserDTO createUpdateUserDto() {
    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setCurrentPassword(TestConstants.PASSWORD);
    dto.setNewPassword(TestConstants.PASSWORD);

    return dto;
  }

  public static CaptchaDTO createCaptchaDto() {
    CaptchaDTO captchaDTO = new CaptchaDTO();
    captchaDTO.setCaptchaResponse(UUID.randomUUID().toString());

    return captchaDTO;
  }

  public static CaptchaResponseDTO createCaptchaResponseDTO(boolean success, double score) {
    CaptchaResponseDTO dto = new CaptchaResponseDTO();
    dto.setSuccess(success);
    dto.setScore(score);

    return dto;
  }

  public static UserDTO createUserDto() {
    UserDTO dto = new UserDTO();
    dto.setEmail(TestConstants.EMAIL);
    dto.setUsername(TestConstants.USERNAME);

    return dto;
  }

}
