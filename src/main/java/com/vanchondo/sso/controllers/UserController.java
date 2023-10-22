package com.vanchondo.sso.controllers;

import com.vanchondo.sso.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/user")
@Log4j2
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(value="/available")
    public ResponseEntity<Void> available(@RequestParam(value = "username", required = false) String username, @RequestParam(value="email", required = false) String email) {
        return userService.available(username, email)
            ? ResponseEntity.ok().build()
            : ResponseEntity.badRequest().build();
    }
}
