package com.vanchondo.sso.controllers;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.sasl.AuthenticationException;
import javax.validation.Valid;

@RestController
@RequestMapping("/")
public class LoginController {

    private final static Logger logger = LogManager.getLogger();

    private UserService userService;
    private AuthenticationService authenticationService;

    public LoginController(UserService userService, AuthenticationService authenticationService){
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping(value = "register")
    public ResponseEntity<UserDTO> saveUser(@Valid @RequestBody SaveUserDTO user){
        UserDTO dto = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("login")
    public TokenDTO login(@RequestBody LoginDTO login) throws AuthenticationException {
        logger.info("::login::Entering login endpoint for username={}", login.getUsername());
        return authenticationService.login(login);
    }

    @GetMapping("currentUser")
    public CurrentUserDTO getCurrentUser(@RequestAttribute("currentUser") CurrentUserDTO currentUser){
        logger.info("::getCurrentUser::Entering getCurrentUser endpoint for username={}", currentUser.getUsername());
        return currentUser;
    }
}
