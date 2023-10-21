package com.vanchondo.sso.services;

import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.dtos.users.DeleteUserDTO;
import com.vanchondo.sso.dtos.users.UpdateUserDTO;
import com.vanchondo.sso.dtos.users.UserDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.ConflictException;
import com.vanchondo.sso.exceptions.NotFoundException;
import com.vanchondo.sso.mappers.UserDTOMapper;
import com.vanchondo.sso.repositories.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

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
        String methodName = "::deleteUser::";
        UserEntity entity = userRepository.findByUsername(currentUser.getUsername());
        if (entity == null) {
            log.warn("{}User not found for deletion. user={}", methodName, currentUser.getUsername());
            throw new NotFoundException("User not found");
        }
        else {
            if (passwordEncoder.matches(dto.getPassword(), entity.getPassword())){
                userRepository.delete(entity);
                log.info("{}User deleted successfully. user={}", methodName, currentUser.getUsername());
                return true;
            }
            else {
                log.warn("{}User cannot be deleted, password is not valid. user={}", methodName, currentUser.getUsername());
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