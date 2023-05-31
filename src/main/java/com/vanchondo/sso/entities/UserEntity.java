package com.vanchondo.sso.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("users")
@Data
public class UserEntity {
    @Id
    private String username;
    @Indexed(unique=true)
    private String email;
    private String password;
    private boolean isActive;
    private LocalDateTime lastUpdatedAt;
}
