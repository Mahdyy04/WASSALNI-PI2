package com.example.booking.service;

import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.entities.Booking;
import com.example.booking.enums.BookingStatus;
import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public BookingResponse bookRide(CreateBookingRequest request) {

        Booking booking = new Booking();
        booking.setRideId(request.getRideId());
        booking.setPassengerId(request.getPassengerId());
        // Ensure seats is at least 1
        int seats = (request.getSeats() != null && request.getSeats() > 0) ? request.getSeats() : 1;
        booking.setSeatsBooked(seats);
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        BookingResponse response = new BookingResponse();
        response.setBookingId(saved.getId());
        response.setRideId(saved.getRideId());
        response.setPassengerId(saved.getPassengerId());
        response.setSeatsBooked(saved.getSeatsBooked());
        response.setStatus(saved.getStatus());

        return response;
    }

    @Override
    public void cancelBooking(String bookingId, String passengerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getPassengerId().equals(passengerId)) {
            throw new RuntimeException("You cannot cancel another user's booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    @Override
    public void acceptBooking(String bookingId, String driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify that the driver owns the ride
        if (!verifyDriverOwnsRide(booking.getRideId(), driverId)) {
            throw new RuntimeException("You cannot accept bookings for rides you don't own");
        }

        int seatsToDeduct = booking.getSeatsBooked() != null ? booking.getSeatsBooked() : 1;
        
        // Update booking status
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Update available seats in ride service
        try {
            System.out.println("Updating ride seats: rideId=" + booking.getRideId() + ", seatsToDeduct=" + seatsToDeduct);
            
            webClientBuilder.build()
                    .put()
                    .uri("http://ride-service/api/rides/{rideId}/seats?seatsToDeduct={seats}",
                            booking.getRideId(), seatsToDeduct)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            System.out.println("Successfully updated ride seats");
        } catch (Exception e) {
            // Log error but don't fail the booking acceptance
            System.err.println("Warning: Could not update ride seats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void rejectBooking(String bookingId, String driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Verify that the driver owns the ride
        if (!verifyDriverOwnsRide(booking.getRideId(), driverId)) {
            throw new RuntimeException("You cannot reject bookings for rides you don't own");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    /**
     * Verifies that the given driver owns the specified ride.
     */
    private boolean verifyDriverOwnsRide(String rideId, String driverId) {
        try {
            java.util.Map<?, ?> ride = webClientBuilder.build()
                    .get()
                    .uri("http://ride-service/api/rides/{rideId}", rideId)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .block();
            
            if (ride == null) {
                return false;
            }
            
            String rideDriverId = (String) ride.get("driverId");
            return driverId.equals(rideDriverId);
        } catch (Exception e) {
            System.err.println("Error verifying ride ownership: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Booking> getBookingsByPassenger(String passengerId) {
        return bookingRepository.findByPassengerId(passengerId);
    }

    @Override
    public List<Booking> getBookingsByRide(String rideId) {
        return bookingRepository.findByRideId(rideId);
    }

    @Override
    public List<Booking> getPendingBookingsByDriver(String driverId) {
        try {
            // Call ride-service to get all rides for this driver
            List<?> driverRides = webClientBuilder.build()
                    .get()
                    .uri("http://ride-service/api/rides/driver/{driverId}", driverId)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (driverRides == null || driverRides.isEmpty()) {
                return List.of();
            }

            // Extract ride IDs from driver's rides
            List<String> driverRideIds = driverRides.stream()
                    .filter(ride -> ride instanceof java.util.Map)
                    .map(ride -> (String) ((java.util.Map<?, ?>) ride).get("id"))
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            // Filter pending bookings to only those for this driver's rides
            return bookingRepository.findAll().stream()
                    .filter(b -> b.getStatus() == BookingStatus.PENDING)
                    .filter(b -> driverRideIds.contains(b.getRideId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching driver rides: " + e.getMessage());
            // Return empty list on error to avoid showing other drivers' bookings
            return List.of();
        }
    }
}