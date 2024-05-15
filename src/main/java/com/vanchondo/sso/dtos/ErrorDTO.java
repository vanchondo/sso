package com.vanchondo.sso.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO {
  private String error;
  private int statusCode;
  private String path;
  private List<String> messages;
}
