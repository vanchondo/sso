package com.vanchondo.sso.filters;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.exceptions.AuthenticationException;
import com.vanchondo.sso.mappers.CurrentUserDTOMapper;
import com.vanchondo.sso.services.AuthenticationService;
import com.vanchondo.sso.utilities.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class JwtFilter implements WebFilter {

    private final LoginConfiguration loginConfiguration;

    public JwtFilter(LoginConfiguration loginConfiguration){
        this.loginConfiguration = loginConfiguration;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String invalidTokenMessage = "Invalid Authorization Token";
        if (isUnsecuredUrl(request.getURI().getPath(), Objects.requireNonNull(request.getMethod()).toString())) {
            return chain.filter(exchange);
        } else if (HttpMethod.OPTIONS == request.getMethod()) {
            response.setStatusCode(HttpStatus.OK);
            return chain.filter(exchange);
        } else {
            String authHeader = Optional.ofNullable(request.getHeaders().get(HttpHeaders.AUTHORIZATION))
              .orElse(Collections.emptyList())
              .stream().findFirst().orElse(Strings.EMPTY);

            String authParam = Optional.ofNullable(request.getQueryParams().get("token"))
              .orElse(Collections.emptyList())
              .stream().findFirst().orElse(Strings.EMPTY);

            if (StringUtils.isEmpty(authParam) && (StringUtils.isEmpty(authHeader)
              || !authHeader.startsWith(Constants.BEARER_VALUE))) {
                // Write the error response
                return Mono.error(new AuthenticationException(invalidTokenMessage));

            }
            String token = StringUtils.isEmpty(authHeader) ? authParam : authHeader.substring(7);

            try {
                final Claims claims = Jwts.parserBuilder()
                  .setSigningKey(
                    AuthenticationService.getSigningKey(loginConfiguration.getSecretKey())
                  ).build()
                  .parseClaimsJws(token)
                  .getBody();
                CurrentUserDTO currentUser = CurrentUserDTOMapper.map(claims);
                exchange.getAttributes().put(Constants.CURRENT_USER_ATTRIBUTE, currentUser);
//                response.addHeader("Access-Control-Expose-Headers", "Authorization");
//                response.addHeader("Authorization", usersService.generateToken(currentUser).getToken());
            } catch (Exception e) {
                // Write the error response
                return Mono.error(new AuthenticationException(invalidTokenMessage));
            }

            return chain.filter(exchange);
        }
    }

    private boolean isUnsecuredUrl(String path, String requestMethod) {
        String cleanedUrl = cleanUrl(path);

        return loginConfiguration.getUnsecuredUrls().stream()
          .anyMatch(urlResource -> {
              if (urlResource.getUrl().endsWith("*")){
                  String url = urlResource.getUrl().substring(0, urlResource.getUrl().length()-1);
                  return cleanedUrl.contains(url)
                    && urlResource.getMethods().contains(requestMethod);
              }
              else {
                  return cleanedUrl.equals(urlResource.getUrl())
                    && urlResource.getMethods().contains(requestMethod);
              }
          });
    }

    private String cleanUrl(String url) {
        StringBuilder urlString = new StringBuilder(url);

        if (urlString.length() > 0 && urlString.charAt(urlString.length() - 1) == '/') {
            urlString.deleteCharAt(urlString.length() - 1);
        }
        return urlString.toString();
    }
}