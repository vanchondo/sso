package com.vanchondo.sso.dtos.security;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CaptchaDTO {
    @NotEmpty
    private String captchaResponse;
    private boolean test;
}
