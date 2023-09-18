package com.vanchondo.sso.routes;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.vanchondo.sso.handlers.LoginHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class LoginRoutes {

  @Bean
  public RouterFunction<ServerResponse> loginRoutes(LoginHandler loginHandler) {
    return route(POST("/register"), loginHandler::handleRegister);
  }
}
