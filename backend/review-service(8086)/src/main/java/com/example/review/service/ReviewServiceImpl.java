package com.example.review.service;

import com.example.review.dto.CreateReviewRequest;
import com.example.review.entities.Review;
import com.example.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Review createReview(CreateReviewRequest request) {
        Review review = new Review();
        review.setReviewerId(request.getReviewerId());
        review.setReviewedId(request.getReviewedId());
        review.setRideId(request.getRideId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setType(request.getType());
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsByReviewedUser(String userId) {
        return reviewRepository.findByReviewedId(userId);
    }

    @Override
    public List<Review> getReviewsByReviewer(String reviewerId) {
        return reviewRepository.findByReviewerId(reviewerId);
    }

    @Override
    public List<Review> getReviewsByRide(String rideId) {
        return reviewRepository.findByRideId(rideId);
    }

    @Override
    public Double getAverageRating(String userId) {
        List<Review> reviews = reviewRepository.findByReviewedId(userId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public List<Review> getReviewsByReviewedUserAndType(String userId, Review.ReviewType type) {
        return reviewRepository.findByReviewedIdAndType(userId, type);
    }
}