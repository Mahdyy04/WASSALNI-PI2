package com.example.ride.entities;

import com.example.ride.enums.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "rides")
public class Ride {

    @Id
    private String id;

    private City departureCity;
    private City destinationCity;

    private LocalDate departureDate;
    private Integer availableSeats;
    private Double pricePerSeat;

    private RideStatus status = RideStatus.SCHEDULED;

    private String driverId;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
