package com.vanchondo.sso.dtos.security;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CaptchaDTO {
    @NotEmpty
    private String captchaResponse;
}
