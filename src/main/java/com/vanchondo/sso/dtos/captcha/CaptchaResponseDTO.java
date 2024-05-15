package com.vanchondo.sso.dtos.captcha;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CaptchaResponseDTO {
  private boolean success;

  @JsonProperty("challenge_ts")
  private String challengeTs;

  private String hostname;

  @JsonProperty("error-codes")
  private ErrorCodeEnum[] errorCodes;

  private double score;
  private String action;

  @JsonIgnore
  public boolean hasClientError() {
    ErrorCodeEnum[] errors = getErrorCodes();
    if (errors == null) {
      return false;
    }
    for (ErrorCodeEnum error : errors) {
      switch (error) {
        case InvalidResponse:
        case MissingResponse:
          return true;
      }
    }
    return false;
  }
}
