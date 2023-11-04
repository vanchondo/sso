package com.vanchondo.sso.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureEntity {
  private String type;
  private byte[] picture;
}
