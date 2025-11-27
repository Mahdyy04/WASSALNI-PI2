package com.example.authentication.repos;

import com.example.authentication.entities.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DriverRepo extends MongoRepository<Driver, String> {

}
