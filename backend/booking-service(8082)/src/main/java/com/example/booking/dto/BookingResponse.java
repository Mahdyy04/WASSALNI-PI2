package com.example.booking.dto;

import com.example.booking.enums.BookingStatus;
import lombok.Data;

@Data
public class BookingResponse {
    private String bookingId;
    private String rideId;
    private String passengerId;
    private Integer seatsBooked;
    private BookingStatus status;
}
