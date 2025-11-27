package com.example.ride.service;

import com.example.ride.dto.CreateRideRequest;
import com.example.ride.entities.Ride;

import java.util.List;

public interface RideService {
    Ride createRide(CreateRideRequest request);
    void deleteRide(String rideId, String driverId);
    List<Ride> getRidesByDriver(String driverId);
    List<Ride> searchRides(String departureCity, String destinationCity, java.time.LocalDate date);
    List<Ride> getAllRides();
    Ride getRideById(String rideId);
}