package com.example.authentication.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class AppUser {

    public enum Gender {
        MALE,
        FEMALE
    }

    public enum UserType {
        PASSENGER,
        DRIVER,
        ADMIN
    }

    @Id
    private String id;

    private String email;
    private String password;
    private String phoneNumber;

    private Gender gender;
    private UserType userType;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    private Boolean isBanned = false;
}
