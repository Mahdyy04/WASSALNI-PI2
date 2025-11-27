package com.example.review.service;

import com.example.review.dto.CreateReviewRequest;
import com.example.review.entities.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(CreateReviewRequest request);
    List<Review> getReviewsByReviewedUser(String userId);
    List<Review> getReviewsByReviewer(String reviewerId);
    List<Review> getReviewsByRide(String rideId);
    Double getAverageRating(String userId);
    List<Review> getReviewsByReviewedUserAndType(String userId, Review.ReviewType type);
}
