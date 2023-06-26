package com.vanchondo.sso.utilities;
public abstract class RegexConstants {
  public static final String PASSWORD_REGEX = "^.{7,50}$";
  public static final String USERNAME_REGEX = "^[A-Za-z][A-Za-z0-9_]{5,29}$";
}
