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
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO saveUser(SaveUserDTO dto){
        if (userRepository.findByEmail(dto.getEmail()) == null) {
            UserEntity entity = UserEntityMapper.map(dto);
            // TODO send email to validate it
            entity.setActive(true);
            entity.setLastUpdatedAt(LocalDateTime.now());
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));

            entity = userRepository.save(entity);
            return UserDTOMapper.map(entity);
        }
        else {
            throw new ConflictException("Email is already registered");
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