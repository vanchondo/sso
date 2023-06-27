package com.vanchondo.sso.exceptions;

public class ReCaptchaInvalidException extends RuntimeException {
  public ReCaptchaInvalidException(String msg) {
    super(msg);
  }
}
