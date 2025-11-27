package com.example.authentication.repos;

import com.example.authentication.entities.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepo extends MongoRepository<AppUser, String> {

    Optional<AppUser> findByEmail(String email);
}
