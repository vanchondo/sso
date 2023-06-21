package com.vanchondo.sso.services;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.SaveUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.ConflictException;
import com.vanchondo.sso.exceptions.NotFoundException;
import com.vanchondo.sso.mappers.UserDTOMapper;
import com.vanchondo.sso.mappers.UserEntityMapper;
import com.vanchondo.sso.repositories.UserRepository;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class UserService {
    private EmailService emailService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserDTO saveUser(SaveUserDTO dto){
        if (userRepository.findByEmail(dto.getEmail()) == null && userRepository.findByUsername(dto.getUsername()) == null) {
            UserEntity entity = UserEntityMapper.map(dto);
            entity.setActive(false);
            entity.setLastUpdatedAt(LocalDateTime.now());
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
            entity.setVerificationToken(UUID.randomUUID().toString());
            try {
                entity = userRepository.save(entity);
                emailService.sendEmail(entity.getEmail(), entity.getVerificationToken());
            } catch (MessagingException | TemplateException | IOException e) {
                userRepository.delete(entity);
                log.error("::saveUser:: Error sending email to={}", entity.getEmail(), e);
                throw new ConflictException("Error sending email to=" + entity.getEmail());
            }
            return UserDTOMapper.map(entity);
        }
        else {
            throw new ConflictException("Email or username already registered");
        }
    }

    public void validateUser(String email, String token) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(email)) {
            //. Do not return error to avoid data leaking
            return;
        }
        UserEntity entity = userRepository.findByEmail(email);
        if (entity == null) {
            //. Do not return error to avoid data leaking
            return;
        }
        if (!entity.isActive() && token.equals(entity.getVerificationToken())) {
            entity.setActive(true);
            entity.setLastUpdatedAt(LocalDateTime.now());
            entity.setVerificationToken(null);
            userRepository.save(entity);
        }
    }

    public UserDTO updateUser(UpdateUserDTO dto, CurrentUserDTO currentUser){
        UserEntity entity = userRepository.findByUsername(currentUser.getUsername());
        if (entity == null) {
            throw new NotFoundException("User not found");
        }
        else {
            if (passwordEncoder.matches(dto.getCurrentPassword(), entity.getPassword())){
                entity.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                entity.setLastUpdatedAt(LocalDateTime.now());

                entity = userRepository.save(entity);
                return UserDTOMapper.map(entity);
            }
            else {
                throw new ConflictException("Password is not valid");
            }
        }
    }

    public boolean deleteUser(DeleteUserDTO dto, CurrentUserDTO currentUser){
        UserEntity entity = userRepository.findByUsername(currentUser.getUsername());
        if (entity == null) {
            throw new NotFoundException("User not found");
        }
        else {
            if (passwordEncoder.matches(dto.getPassword(), entity.getPassword())){
                userRepository.delete(entity);
                return true;
            }
            else {
                throw new ConflictException("Password is not valid");
            }
        }
    }

    public boolean available(String username, String email){
        try {
            if (!StringUtils.isEmpty(username)){
                findUserByUsername(username);
            }
            else if (!StringUtils.isEmpty(email)) {
                findByEmail(email);
            }
        } catch (NotFoundException ex){
            return true;
        }
        return false;
    }

    public UserDTO findUserByUsername(String username) {
        return UserDTOMapper.map(findUserEntityByUsername(username));
    }

    public UserEntity findUserEntityByUsername(String username) {
        UserEntity entity = userRepository.findByUsername(username);
        if (entity == null) {
            throw new NotFoundException("User not found");
        }

        return entity;
    }

    public UserDTO findByEmail(String email) {
        UserEntity entity = userRepository.findByEmail(email);
        if (entity == null) {
            throw new NotFoundException("User not found");
        }

        return UserDTOMapper.map(entity);
    }
}