package com.vanchondo.sso.dtos.security;

import java.util.Date;
import lombok.Data;

@Data
public class CurrentUserDTO {
    private String iss;
    private String username;
    private String email;
    private String role;
    private Date iat;
    private Date exp;
}
