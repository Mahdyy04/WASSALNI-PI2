package com.example.authentication.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "drivers")
public class Driver extends AppUser {

    private String licenseNumber;
    private String vehicleNumber;
    private String vehiclePlate;
    private Boolean isVerified;
    private Double rating;
}
