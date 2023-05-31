package com.vanchondo.sso.mappers;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Optional;

public class CurrentUserDTOMapper {

    public static CurrentUserDTO map(Claims claims) {
        return Optional.of(claims)
                .map(claim -> {
                    CurrentUserDTO cu = new CurrentUserDTO();

                    cu.setIss(Optional.of(claim)
                            .map(Claims::getIssuer)
                            .orElse(StringUtils.EMPTY));

                    cu.setUsername(Optional.of(claim)
                            .map(c->c.get("username").toString())
                            .orElse(StringUtils.EMPTY));

                    cu.setEmail(Optional.of(claim)
                            .map(Claims::getSubject)
                            .orElse(StringUtils.EMPTY));

                    cu.setIat(Optional.of(claim)
                            .map(Claims::getIssuedAt)
                            .orElse(new Date()));

                    cu.setExp(Optional.of(claim)
                            .map(Claims::getExpiration)
                            .orElse(new Date()));

                    cu.setRole(Optional.ofNullable(claim.get("role"))
                            .map(String::valueOf)
                            .orElse(StringUtils.EMPTY));

                    return cu;
                })
                .orElse(new CurrentUserDTO());
    }
}
