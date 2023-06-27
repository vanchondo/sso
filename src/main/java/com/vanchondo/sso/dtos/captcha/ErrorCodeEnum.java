package com.vanchondo.sso.dtos.captcha;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;

public enum ErrorCodeEnum {
  MissingSecret,
  InvalidSecret,
  MissingResponse,
  InvalidResponse;

  private static Map<String, ErrorCodeEnum> errorsMap = new HashMap<>(4);

  static {
    errorsMap.put("missing-input-secret", MissingSecret);
    errorsMap.put("invalid-input-secret", InvalidSecret);
    errorsMap.put("missing-input-response", MissingResponse);
    errorsMap.put("invalid-input-response", InvalidResponse);
  }

  @JsonCreator
  public static ErrorCodeEnum forValue(String value) {
    return errorsMap.get(value.toLowerCase());
  }
}
