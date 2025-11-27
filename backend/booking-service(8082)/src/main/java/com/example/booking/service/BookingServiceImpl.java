package com.example.booking.service;

import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.entities.Booking;
import com.example.booking.enums.BookingStatus;
import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    public BookingResponse bookRide(CreateBookingRequest request) {

        Booking booking = new Booking();
        booking.setRideId(request.getRideId());
        booking.setPassengerId(request.getPassengerId());
        booking.setSeatsBooked(request.getSeats());
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

        // TODO: Verify driver owns the ride
        // This would require calling ride-service to check ride ownership

        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Override
    public void rejectBooking(String bookingId, String driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // TODO: Verify driver owns the ride
        // This would require calling ride-service to check ride ownership

        booking.setStatus(BookingStatus.REJECTED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
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
    // Pour l'instant, retourne tous les bookings PENDING
    // En production, il faudrait appeler ride-service pour filtrer par driver
    return bookingRepository.findAll().stream()
            .filter(b -> b.getStatus() == BookingStatus.PENDING)
            .collect(Collectors.toList());
}
}