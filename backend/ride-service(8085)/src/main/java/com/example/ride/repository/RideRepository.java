package com.example.ride.repository;

import com.example.ride.entities.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RideRepository extends MongoRepository<Ride, String> {
    List<Ride> findByDriverId(String driverId);
}
