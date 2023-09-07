package com.vanchondo.sso.dtos.users;

import com.vanchondo.sso.dtos.security.CaptchaDTO;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class DeleteUserDTO extends CaptchaDTO {

    @NotNull (message = "Password is required")
    @Size(min = 6, max = 50, message = "Password not valid, min=6, max=50")
    private String password;
}
