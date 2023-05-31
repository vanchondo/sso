package com.vanchondo.sso.dtos.users;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UpdateUserDTO {

    @NotNull (message = "CurrentPassword is required")
    @Size(min = 6, max = 50, message = "CurrentPassword not valid, min=6, max=50")
    private String currentPassword;

    @NotNull (message = "NewPassword is required")
    @Size(min = 6, max = 50, message = "NewPassword not valid, min=6, max=50")
    private String newPassword;
}
