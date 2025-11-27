package com.example.authentication.dto;

import com.example.authentication.entities.AppUser;

public record CreateAccountRequest(
        String email,
        String password,
        String phoneNumber,
        AppUser.Gender gender,
        AppUser.UserType userType,

        // Driver fields
        String licenseNumber,
        String vehicleNumber,
        String vehiclePlate,

        // Passenger fields
        String preferredPaymentMethod
) {}
