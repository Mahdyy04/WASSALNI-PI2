package com.example.booking.service;

import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.entities.Booking;

import java.util.List;

public interface BookingService {

    BookingResponse bookRide(CreateBookingRequest request);

    void cancelBooking(String bookingId, String passengerId);

    void acceptBooking(String bookingId, String driverId);

    void rejectBooking(String bookingId, String driverId);

    List<Booking> getBookingsByPassenger(String passengerId);

    List<Booking> getBookingsByRide(String rideId);

    List<Booking> getPendingBookingsByDriver(String driverId);
}