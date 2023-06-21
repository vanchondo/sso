package com.vanchondo.sso.services;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.AuthenticationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final static Logger logger = LogManager.getLogger();

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LoginConfiguration loginConfiguration;

    public AuthenticationService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            LoginConfiguration loginConfiguration
    ){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.loginConfiguration = loginConfiguration;
    }

    public TokenDTO login(LoginDTO login) throws AuthenticationException {
        String username = login.getUsername();
        String password = login.getPassword();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            logger.warn("Invalid email or password for username={}", username);
            throw new IllegalArgumentException("Invalid Email or password");
        }

        UserEntity user = userService.findUserEntityByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Invalid email or password for username={}", username);
            throw new AuthenticationException("Email or password incorrect");
        }
        if (!user.isActive()) {
            logger.warn("User is not active, username={}", username);
            throw new AuthenticationException("User is not active");
        }

        return generateToken(user);
    }

//    public String getEncodedPassword(String password) {
//        return passwordEncoder.encode(password);
//    }

    private TokenDTO generateToken(UserEntity currentUser) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, loginConfiguration.getExpirationToken());


        return new TokenDTO(Jwts.builder()
                .setIssuer(loginConfiguration.getIssuer())
                .setSubject(currentUser.getEmail())
                .claim("username", currentUser.getUsername())
//            .claim("role", currentUser.getRole().getName())
//            .claim("authorities", getListOfAuthorities(currentUser.getRole().getAuthorities()))
//            .claim("store", currentUser.getStore().toString())
                .setIssuedAt(new Date())
                .setExpiration(cal.getTime())
                .signWith(
                        getSigningKey(loginConfiguration.getSecretKey()),
                        SignatureAlgorithm.HS256
                )
                .compact());
    }

    public static Key getSigningKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
