package com.example.review.repository;

import com.example.review.entities.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByReviewedId(String reviewedId);
    List<Review> findByReviewerId(String reviewerId);
    List<Review> findByRideId(String rideId);
    List<Review> findByReviewedIdAndType(String reviewedId, Review.ReviewType type);
}
