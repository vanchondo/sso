package com.vanchondo.sso.utilities;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

public abstract class NetworkUtil {

  public static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";

  private NetworkUtil() {}

  public static String getClientIp(HttpServletRequest request) {
    if (request != null) {
      String remoteAddr = request.getHeader(X_FORWARDED_FOR);
      if (StringUtils.isEmpty(remoteAddr)) {
        return request.getRemoteAddr();
      }
    }
    return Strings.EMPTY;
  }
}
