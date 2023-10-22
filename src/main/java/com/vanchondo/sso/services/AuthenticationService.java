package com.vanchondo.sso.services;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.dtos.security.LoginDTO;
import com.vanchondo.sso.dtos.security.TokenDTO;
import com.vanchondo.sso.entities.UserEntity;
import com.vanchondo.sso.exceptions.AuthenticationException;
import com.vanchondo.sso.exceptions.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@AllArgsConstructor
public class AuthenticationService {

    private final ReactiveUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LoginConfiguration loginConfiguration;

    public Mono<TokenDTO> login(LoginDTO login) throws AuthenticationException {
        String username = login.getUsername();
        String password = login.getPassword();

        return userService.findUserEntityByUsername(username)
          .defaultIfEmpty(new UserEntity())
          .flatMap(user -> {
              if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                  log.warn("::login::Invalid email or password for username={}", username);
                  return Mono.error(new AuthenticationException("Email or password incorrect"));
              }
              if (!user.isActive()) {
                  log.warn("::login::User is not active, username={}", username);
                  return Mono.error(new AuthenticationException("User is not active"));
              }
              return Mono.just(generateToken(user));
          }).onErrorResume(NotFoundException.class, ex -> {
              log.warn("::login:: User not found", ex);
              return Mono.error(new AuthenticationException("Email or password incorrect"));
          });
    }

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
