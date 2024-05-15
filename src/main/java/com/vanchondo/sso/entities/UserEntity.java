package com.vanchondo.sso.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@Data
public class UserEntity implements Serializable {
  @Id private String username;

  @Indexed(unique = true)
  private String email;

  private String password;
  private String verificationToken;
  private boolean isActive;
  private LocalDateTime lastUpdatedAt;
  private PictureEntity profilePicture;
}
