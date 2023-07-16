package com.vanchondo.sso.dtos.security;

import com.vanchondo.sso.utilities.RegexConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginDTO extends CaptchaDTO {
    @NotNull(message = "Username is required")
    @Pattern(regexp = RegexConstants.USERNAME_REGEX, message = "Username not valid min=6, max=29")
    private String username;

    @NotNull (message = "Password is required")
    @Pattern(regexp = RegexConstants.PASSWORD_REGEX, message = "Password not valid, min=6, max=50" )
    private String password;
}
