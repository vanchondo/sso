package com.vanchondo.sso.controllers;

import com.vanchondo.sso.aspect.ValidateCaptcha;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.services.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import javax.security.sasl.AuthenticationException;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/")
@AllArgsConstructor
@Log4j2
public class LoginController {
    private final AuthenticationService authenticationService;

    @ValidateCaptcha
    @PostMapping("login")
    public TokenDTO login(ServerWebExchange exchange, @Valid @RequestBody LoginDTO login) throws AuthenticationException {
        log.info("::login::Entering login endpoint for username={}", login.getUsername());
        return authenticationService.login(login);
    }
}
