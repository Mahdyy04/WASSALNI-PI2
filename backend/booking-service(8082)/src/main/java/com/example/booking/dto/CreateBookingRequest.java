package com.example.booking.dto;

import lombok.Data;

@Data
public class CreateBookingRequest {
    private String rideId;
    private String passengerId;
    private Integer seats ;
}
