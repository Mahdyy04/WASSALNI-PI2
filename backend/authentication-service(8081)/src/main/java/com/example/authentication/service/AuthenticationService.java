package com.example.authentication.service;

import com.example.authentication.dto.CreateAccountRequest;
import com.example.authentication.entities.Driver;
import com.example.authentication.entities.Passenger;
import com.example.authentication.entities.AppUser;
import com.example.authentication.repos.DriverRepo;
import com.example.authentication.repos.PassengerRepo;
import com.example.authentication.repos.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepo userRepo;
    private final DriverRepo driverRepo;
    private final PassengerRepo passengerRepo;

    public AppUser createAccount(CreateAccountRequest request) {

        if (userRepo.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already taken");
        }

        AppUser user;

        if (request.userType() == AppUser.UserType.DRIVER) {
            Driver d = new Driver();
            d.setLicenseNumber(request.licenseNumber());
            d.setVehicleNumber(request.vehicleNumber());
            d.setVehiclePlate(request.vehiclePlate());
            d.setIsVerified(false);
            d.setRating(0.0);
            user = d;
        } else {
            Passenger p = new Passenger();
            p.setPreferredPaymentMethod(request.preferredPaymentMethod());
            p.setRating(0.0);
            user = p;
        }

        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setPhoneNumber(request.phoneNumber());
        user.setGender(request.gender());
        user.setUserType(request.userType());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepo.save(user);
    }

    public boolean authenticate(String email, String password) {
        return userRepo.findByEmail(email)
                .map(u -> u.getPassword().equals(password) && !Boolean.TRUE.equals(u.getIsBanned()))
                .orElse(false);
    }

    public java.util.List<AppUser> getAllUsers() {
        return userRepo.findAll();
    }

    public AppUser getUserById(String userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AppUser getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AppUser banUser(String userId) {
        AppUser user = getUserById(userId);
        user.setIsBanned(true);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }

    public AppUser unbanUser(String userId) {
        AppUser user = getUserById(userId);
        user.setIsBanned(false);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }
}