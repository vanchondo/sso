package com.vanchondo.sso.dtos.security;

import com.vanchondo.sso.utilities.RegexConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidateUserDTO extends CaptchaDTO {
    @NotNull (message = "Email is required")
    @Pattern(regexp = RegexConstants.EMAIL_REGEX, message = "Email not valid")
    private String email;
    @NotNull
    @NotEmpty
    private String token;
}