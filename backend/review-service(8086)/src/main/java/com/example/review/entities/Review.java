package com.example.review.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    private String reviewerId;
    private String reviewedId;
    private String rideId;

    private Integer rating;
    private String comment;

    private ReviewType type;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ReviewType {
        DRIVER,
        PASSENGER
    }
}
