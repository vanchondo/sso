package com.vanchondo.sso.controllers;

import com.vanchondo.sso.aspect.ValidateCaptcha;
import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/user")
@Log4j2
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping(value = "")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UpdateUserDTO user, @RequestAttribute("currentUser") CurrentUserDTO currentUser){
        UserDTO dto = userService.updateUser(user, currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @ValidateCaptcha
    @DeleteMapping(value = "")
    public ResponseEntity<Void> deleteUser(@Valid @RequestBody DeleteUserDTO user, @RequestAttribute("currentUser") CurrentUserDTO currentUser) {
        log.info("::deleteUser::Entering deleteUser endpoint. user={}", currentUser.getUsername());
        return userService.deleteUser(user, currentUser)
            ? ResponseEntity.status(HttpStatus.OK).build()
            : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value="/available")
    public ResponseEntity<Void> available(@RequestParam(value = "username", required = false) String username, @RequestParam(value="email", required = false) String email) {
        return userService.available(username, email)
            ? ResponseEntity.ok().build()
            : ResponseEntity.badRequest().build();
    }
}
