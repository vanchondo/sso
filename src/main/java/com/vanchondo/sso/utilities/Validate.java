package com.vanchondo.sso.utilities;

import org.springframework.stereotype.Component;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@AllArgsConstructor
public class Validate {
  private final Validator validator;

  public Mono<Object> validate(Object dto) {
    String logPrefix = "::validate::";
    Set<ConstraintViolation<Object>> violations = validator.validate(dto);
    if (!violations.isEmpty()){
      log.info("{}Object is invalid", logPrefix);
//      return Mono.error(new ConstraintViolationException(violations));
      throw new ConstraintViolationException(violations);
    }

    log.info("{}Object is valid", logPrefix);
    return Mono.just(dto);
  }
}
