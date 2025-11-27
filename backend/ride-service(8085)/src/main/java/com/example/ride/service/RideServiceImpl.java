package com.example.ride.service;

import com.example.ride.dto.CreateRideRequest;
import com.example.ride.entities.Ride;
import com.example.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;

    @Override
    public Ride createRide(CreateRideRequest request) {
        Ride ride = new Ride();
        ride.setDepartureCity(request.getDepartureCity());
        ride.setDestinationCity(request.getDestinationCity());
        ride.setDepartureDate(request.getDepartureDate());
        ride.setAvailableSeats(request.getAvailableSeats());
        ride.setDriverId(request.getDriverId());

        return rideRepository.save(ride);
    }

    @Override
    public void deleteRide(String rideId, String driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (!ride.getDriverId().equals(driverId)) {
            throw new RuntimeException("You cannot delete someone else's ride");
        }

        rideRepository.delete(ride);
    }

    @Override
    public List<Ride> getRidesByDriver(String driverId) {
        return rideRepository.findByDriverId(driverId);
    }

    @Override
    public List<Ride> searchRides(String departureCity, String destinationCity, java.time.LocalDate date) {
        List<Ride> allRides = rideRepository.findAll();

        return allRides.stream()
                .filter(ride -> {
                    boolean matches = true;
                    if (departureCity != null && !departureCity.isEmpty()) {
                        matches = matches && ride.getDepartureCity() != null &&
                                ride.getDepartureCity().getName() != null &&
                                ride.getDepartureCity().getName().toLowerCase().contains(departureCity.toLowerCase());
                    }
                    if (destinationCity != null && !destinationCity.isEmpty()) {
                        matches = matches && ride.getDestinationCity() != null &&
                                ride.getDestinationCity().getName() != null &&
                                ride.getDestinationCity().getName().toLowerCase().contains(destinationCity.toLowerCase());
                    }
                    if (date != null) {
                        matches = matches && ride.getDepartureDate() != null &&
                                ride.getDepartureDate().equals(date);
                    }
                    return matches && ride.getStatus() == com.example.ride.enums.RideStatus.SCHEDULED &&
                            ride.getAvailableSeats() > 0;
                })
                .toList();
    }

    @Override
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public Ride getRideById(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
    }
}