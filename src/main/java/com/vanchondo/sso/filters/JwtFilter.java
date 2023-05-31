package com.vanchondo.sso.filters;

import com.vanchondo.sso.configs.properties.LoginConfiguration;
import com.vanchondo.sso.dtos.security.CurrentUserDTO;
import com.vanchondo.sso.mappers.CurrentUserDTOMapper;
import com.vanchondo.sso.services.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends GenericFilterBean {

    private final LoginConfiguration loginConfiguration;

    public JwtFilter(LoginConfiguration loginConfiguration){
        this.loginConfiguration = loginConfiguration;
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        String invalidTokenMessage = "Invalid Authorization Token";
        if (isUnsecuredUrl(request.getRequestURI(), request.getMethod())) {
            chain.doFilter(request, response);
        } else if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);

            chain.doFilter(req, res);
        } else {
            final String authHeader = request.getHeader("authorization");
            final String authParam = request.getParameter("token");
            if (StringUtils.isEmpty(authParam) && (authHeader == null || !authHeader
                    .startsWith("Bearer "))) {

                response.sendError(HttpStatus.UNAUTHORIZED.value(), invalidTokenMessage);
                return;
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
                request.setAttribute("currentUser", currentUser);
//                response.addHeader("Access-Control-Expose-Headers", "Authorization");
//                response.addHeader("Authorization", usersService.generateToken(currentUser).getToken());
            } catch (Exception e) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), invalidTokenMessage);
                return;
            }

            chain.doFilter(req, res);
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