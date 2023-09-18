package com.vanchondo.sso.dtos.security;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CaptchaDTO {
    @NotEmpty(message = "CaptchaResponse is required")
    private String captchaResponse;
    private boolean test;
}
