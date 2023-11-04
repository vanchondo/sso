package com.vanchondo.sso.mappers;


import com.vanchondo.sso.entities.PictureEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;


import java.util.Base64;
import java.util.Optional;

public class PictureEntityMapper {
  public static PictureEntity map(String sourceData) {
    if (StringUtils.isEmpty(sourceData)) {
      return null;
    }

    String[] pictureParts = sourceData.split(",");
    String pictureString = pictureParts[1];

    return new PictureEntity(
      getPictureType(pictureParts[0]),
      Base64.getDecoder().decode(pictureString));
  }

  private static String getPictureType(String metadata) {
    // metadata = data:image/jpeg;base64
    return Optional.ofNullable(metadata)
      .map(data -> data.split(":"))
      .map(dataArray -> dataArray[1])
      .map(data -> data.split(";"))
      .map(data -> data[0])
      .orElse(MediaType.IMAGE_JPEG_VALUE);
  }
}
