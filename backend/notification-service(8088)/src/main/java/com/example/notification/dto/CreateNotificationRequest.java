package com.example.notification.dto;

import lombok.Data;

@Data
public class CreateNotificationRequest {
    private String userId;
    private String type;      // BOOKING_REQUEST, BOOKING_ACCEPTED, BOOKING_REJECTED
    private String title;
    private String message;
    private String rideId;
    private String bookingId;
}
