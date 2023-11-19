package com.vanchondo.sso.utilities;

public abstract class LogUtil {
  private LogUtil() {}

  public static String getMethodName(Object ref){
    return String.format("::%s::", ref.getClass().getEnclosingMethod().getName());
  }
}
