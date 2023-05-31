package com.vanchondo.sso.dtos.security;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class CurrentUserDTO {
    private String iss;
    private String username;
    private String email;
    private String role;
    private Date iat;
    private Date exp;
}
