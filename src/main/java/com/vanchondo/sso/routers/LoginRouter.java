package com.vanchondo.sso.routers;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.vanchondo.sso.handlers.LoginHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class LoginRouter {

  @Bean
  public RouterFunction<ServerResponse> loginRoute(LoginHandler loginHandler) {
    return route(POST("/register"), loginHandler::handleRegister)
        .andRoute(GET("/regex"), req -> loginHandler.handleRegex())
        .andRoute(GET("/currentUser"), loginHandler::handleCurrentUser)
        .andRoute(GET("/profilePicture"), loginHandler::handleProfilePicture)
        .andRoute(POST("/validate"), loginHandler::handleValidateUser)
        .andRoute(POST("/login"), loginHandler::handleLogin)
        .andRoute(DELETE("/user"), loginHandler::handleDeleteUser)
        .andRoute(PUT("/user"), loginHandler::handleUpdateUser);
  }
}
