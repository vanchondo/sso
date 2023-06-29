package com.vanchondo.sso.dtos.users;

import com.vanchondo.sso.utilities.RegexConstants;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveUserDTO {
    @NotNull (message = "Username is required")
    @Pattern(regexp = RegexConstants.USERNAME_REGEX, message = "Username not valid min=6, max=29")
    private String username;

    @NotNull (message = "Email is required")
    @Pattern(regexp = RegexConstants.EMAIL_REGEX, message = "Email not valid")
    private String email;

    @NotNull (message = "Password is required")
    @Pattern(regexp = RegexConstants.PASSWORD_REGEX, message = "Password not valid, min=6, max=50" )
    private String password;
}
