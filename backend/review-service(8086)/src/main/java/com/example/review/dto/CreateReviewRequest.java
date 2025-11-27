package com.example.review.dto;

import com.example.review.entities.Review;
import lombok.Data;

@Data
public class CreateReviewRequest {
    private String reviewerId;
    private String reviewedId;
    private String rideId;
    private Integer rating;
    private String comment;
    private Review.ReviewType type;
}