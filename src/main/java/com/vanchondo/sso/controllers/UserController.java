package com.vanchondo.sso.controllers;

import static com.vanchondo.sso.utilities.Sanitize.sanitize;
import static com.vanchondo.sso.utilities.Sanitize.sanitizeCurrentUserDTO;
import static com.vanchondo.sso.utilities.Sanitize.sanitizeDeleteUserDTO;
import static com.vanchondo.sso.utilities.Sanitize.sanitizeUpdateUserDTO;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PutMapping(value = "")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UpdateUserDTO user, @RequestAttribute("currentUser") CurrentUserDTO currentUser){
        sanitizeUpdateUserDTO(user);
        sanitizeCurrentUserDTO(currentUser);
        UserDTO dto = userService.updateUser(user, currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @DeleteMapping(value = "")
    public void deleteUser(@Valid @RequestBody DeleteUserDTO user, @RequestAttribute("currentUser") CurrentUserDTO currentUser){
        sanitizeDeleteUserDTO(user);
        sanitizeCurrentUserDTO(currentUser);
        userService.deleteUser(user, currentUser);
    }

    @GetMapping(value="/available")
    public ResponseEntity<Void> available(@RequestParam(value = "username", required = false) String username, @RequestParam(value="email", required = false) String email) {
        if (userService.available(sanitize(username), sanitize(email))) {
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }
}
