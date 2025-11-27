package com.example.review.controller;

import com.example.review.dto.CreateReviewRequest;
import com.example.review.entities.Review;
import com.example.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    public Review createReview(@RequestBody CreateReviewRequest request) {
        return reviewService.createReview(request);
    }

    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable String userId) {
        return reviewService.getReviewsByReviewedUser(userId);
    }

    @GetMapping("/user/{userId}/average")
    public Map<String, Object> getAverageRating(@PathVariable String userId) {
        Double average = reviewService.getAverageRating(userId);
        return Map.of("userId", userId, "averageRating", average);
    }

    @GetMapping("/ride/{rideId}")
    public List<Review> getReviewsByRide(@PathVariable String rideId) {
        return reviewService.getReviewsByRide(rideId);
    }

    @GetMapping("/user/{userId}/type/{type}")
    public List<Review> getReviewsByUserAndType(
            @PathVariable String userId,
            @PathVariable Review.ReviewType type) {
        return reviewService.getReviewsByReviewedUserAndType(userId, type);
    }
}