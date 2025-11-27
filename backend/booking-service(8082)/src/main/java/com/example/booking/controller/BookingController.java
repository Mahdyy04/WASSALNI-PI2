package com.example.booking.controller;

import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.entities.Booking;
import com.example.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    public BookingResponse createBooking(@RequestBody CreateBookingRequest request) {
        return bookingService.bookRide(request);
    }

    @DeleteMapping("/{bookingId}")
    public String cancelBooking(
            @PathVariable String bookingId,
            @RequestParam String passengerId) {

        bookingService.cancelBooking(bookingId, passengerId);
        return "Booking canceled";
    }

    @GetMapping("/passenger/{passengerId}")
    public List<Booking> getPassengerBookings(@PathVariable String passengerId) {
        return bookingService.getBookingsByPassenger(passengerId);
    }

    @GetMapping("/ride/{rideId}")
    public List<Booking> getRideBookings(@PathVariable String rideId) {
        return bookingService.getBookingsByRide(rideId);
    }

    @PostMapping("/{bookingId}/accept")
    public String acceptBooking(
            @PathVariable String bookingId,
            @RequestParam String driverId) {
        bookingService.acceptBooking(bookingId, driverId);
        return "Booking accepted";
    }

    @PostMapping("/{bookingId}/reject")
    public String rejectBooking(
            @PathVariable String bookingId,
            @RequestParam String driverId) {
        bookingService.rejectBooking(bookingId, driverId);
        return "Booking rejected";
    }

    @GetMapping("/driver/{driverId}/pending")
    public List<Booking> getPendingBookings(@PathVariable String driverId) {
        return bookingService.getPendingBookingsByDriver(driverId);
    }
}