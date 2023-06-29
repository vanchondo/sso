package com.vanchondo.sso.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;

@Log4j2
public class Mapper {
  private static final ObjectMapper mapper = new ObjectMapper();
  public static <T> T readValue(String response, TypeReference<T> referenceType) {
    try {
      return mapper.readValue(response, referenceType);
    } catch (JsonProcessingException e) {
      log.error("::readValue:: ex={}", e.getMessage(), e);
      return null;
    }
  }

  public static String toJson(Object object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error("::toJson:: ex={}", e.getMessage(), e);
      return Strings.EMPTY;
    }
  }
}
