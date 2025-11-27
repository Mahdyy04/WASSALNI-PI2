package com.example.authentication.repos;

import com.example.authentication.entities.Passenger;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PassengerRepo extends MongoRepository<Passenger, String> {
}
