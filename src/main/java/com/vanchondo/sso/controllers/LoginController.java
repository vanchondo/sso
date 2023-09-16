package com.vanchondo.sso.controllers;

import com.vanchondo.sso.aspect.ValidateCaptcha;
import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.dtos.security.ValidateUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.services.ReactiveUserService;
import com.vanchondo.sso.services.UserService;
import com.vanchondo.sso.utilities.RegexConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.sasl.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
@AllArgsConstructor
@Log4j2
public class LoginController {
    private final ReactiveUserService reactiveUserService;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @ValidateCaptcha
    @PostMapping(value = "register")
    public Mono<ResponseEntity<UserDTO>> saveUser(@Valid @RequestBody SaveUserDTO user){
        return reactiveUserService.saveUser(user)
          .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @PostMapping(value = "validate")
    public ResponseEntity<UserDTO> validateUser(@RequestBody ValidateUserDTO user) {
        log.info("::validateUser::Entering validate endpoint for user={}", user);
        userService.validateUser(user);
        return ResponseEntity.ok().build();
    }

    @ValidateCaptcha
    @PostMapping("login")
    public TokenDTO login(@Valid @RequestBody LoginDTO login) throws AuthenticationException {
        log.info("::login::Entering login endpoint for username={}", login.getUsername());
        return authenticationService.login(login);
    }

    @GetMapping("currentUser")
    public CurrentUserDTO getCurrentUser(@RequestAttribute("currentUser") CurrentUserDTO currentUser){
        log.info("::getCurrentUser::Entering getCurrentUser endpoint for username={}", currentUser.getUsername());
        return currentUser;
    }

    @GetMapping("regex")
    public Map<String, String> getRegex(){
        log.info("::getRegex::Entering getRegex endpoint");
        Map<String, String> regexMap = new HashMap<>();
        regexMap.put("USERNAME_REGEX", RegexConstants.USERNAME_REGEX);
        regexMap.put("PASSWORD_REGEX", RegexConstants.PASSWORD_REGEX);
        regexMap.put("EMAIL_REGEX", RegexConstants.EMAIL_REGEX);
        return regexMap;
    }
}
