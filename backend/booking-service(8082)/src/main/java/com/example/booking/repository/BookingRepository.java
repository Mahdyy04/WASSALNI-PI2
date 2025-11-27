package com.example.booking.repository;

import com.example.booking.entities.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByPassengerId(String passengerId);

    List<Booking> findByRideId(String rideId);
}
