package com.vanchondo.sso.mappers;


import com.vanchondo.sso.entities.PictureEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

public class PictureEntityMapper {
  public static PictureEntity map(String sourceData) {
    if (StringUtils.isEmpty(sourceData)) {
      return null;
    }

    String[] pictureParts = sourceData.split(",");
    String pictureString = pictureParts[1];

    return new PictureEntity(pictureParts[0], Base64.getDecoder().decode(pictureString));
  }
}
