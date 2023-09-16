package com.vanchondo.sso.dtos.security;

import com.vanchondo.sso.utilities.RegexConstants;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidateUserDTO extends CaptchaDTO {
    @NotNull(message = "Email is required")
    @Pattern(regexp = RegexConstants.EMAIL_REGEX, message = "Email not valid")
    private String email;
    @NotNull
    @NotEmpty
    private String token;
}