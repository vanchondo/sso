package com.vanchondo.sso.utilities;

import org.apache.logging.log4j.util.Strings;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public abstract class NetworkUtil {

  public static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";

  private NetworkUtil() {}

  public static String getClientIp(ServerWebExchange exchange) {
      return Optional.ofNullable(exchange)
        .map(ServerWebExchange::getRequest)
        .map(ServerHttpRequest::getRemoteAddress)
        .map(InetSocketAddress::getAddress)
        .map(InetAddress::getHostAddress)
        .orElse(Strings.EMPTY);
  }
}
