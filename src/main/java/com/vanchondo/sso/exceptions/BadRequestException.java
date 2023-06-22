package com.vanchondo.sso.exceptions;

public class BadRequestException extends RuntimeException{
  public BadRequestException(String msg) {
    super(msg);
  }
}
