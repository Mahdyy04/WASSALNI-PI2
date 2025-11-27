package com.example.authentication.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "passengers")
public class Passenger extends AppUser {

    private String preferredPaymentMethod;
    private Double rating;
}
