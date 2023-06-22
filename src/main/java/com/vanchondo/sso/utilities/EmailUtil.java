package com.vanchondo.sso.utilities;

import java.net.URLEncoder;
import java.nio.charset.Charset;

public abstract class EmailUtil {

  private EmailUtil(){}

  public static String encode(String text) {
    return URLEncoder.encode(text, Charset.defaultCharset());
  }
}
