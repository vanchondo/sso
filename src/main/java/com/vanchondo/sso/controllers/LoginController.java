package com.vanchondo.sso.controllers;

import static com.vanchondo.sso.utilities.Sanitize.sanitize;
import static com.vanchondo.sso.utilities.Sanitize.sanitizeCurrentUserDTO;
import static com.vanchondo.sso.utilities.Sanitize.sanitizeLoginDto;
import static com.vanchondo.sso.utilities.Sanitize.sanitizeSaveUserDTO;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.exceptions.BadRequestException;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.services.UserService;
import com.vanchondo.sso.utilities.RegexConstants;
import java.util.HashMap;
import java.util.Map;
import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@AllArgsConstructor
@Log4j2
public class LoginController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final CaptchaValidatorService captchaValidatorService;

    @PostMapping(value = "register")
    public ResponseEntity<UserDTO> saveUser(@Valid @RequestBody SaveUserDTO user){
        sanitizeSaveUserDTO(user);
        UserDTO dto = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping(value = "validate")
    public ResponseEntity<UserDTO> validateUser(@RequestParam("email") String email, @RequestParam("token") String token) {
        userService.validateUser(sanitize(email), sanitize(token));
        return ResponseEntity.ok().build();
    }

    @PostMapping("login")
    public TokenDTO login(@Valid @RequestBody LoginDTO login, HttpServletRequest request) throws AuthenticationException {
        log.info("::login::Entering login endpoint for username={}", login.getUsername());
        sanitizeLoginDto(login);
//        if (captchaValidatorService.validateCaptcha(login.getCaptchaResponse(), NetworkUtil.getClientIp(request))) {
        if (captchaValidatorService.validateCaptcha(login.getCaptchaResponse())) {
            return authenticationService.login(login);
        }
        else {
            throw new BadRequestException("Captcha response is incorrect");
        }
    }

    @GetMapping("currentUser")
    public CurrentUserDTO getCurrentUser(@RequestAttribute("currentUser") CurrentUserDTO currentUser){
        log.info("::getCurrentUser::Entering getCurrentUser endpoint for username={}", currentUser.getUsername());
        sanitizeCurrentUserDTO(currentUser);
        return currentUser;
    }

    @GetMapping("regex")
    public Map<String, String> getRegex(){
        log.info("::getRegex::Entering getRegex endpoint");
        Map<String, String> regexMap = new HashMap<>();
        regexMap.put("USERNAME_REGEX", RegexConstants.USERNAME_REGEX);
        regexMap.put("PASSWORD_REGEX", RegexConstants.PASSWORD_REGEX);
        return regexMap;
    }
}
