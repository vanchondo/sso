package com.vanchondo.sso.dtos.security;

import com.vanchondo.sso.utilities.RegexConstants;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginDTO {
    @NotNull(message = "Username is required")
    @Pattern(regexp = RegexConstants.USERNAME_REGEX, message = "Username not valid min=6, max=29")
    private String username;

    @NotNull (message = "Password is required")
    @Pattern(regexp = RegexConstants.PASSWORD_REGEX, message = "Password not valid, min=6, max=50" )
    private String password;

    private String captchaResponse;
}
