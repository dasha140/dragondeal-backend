package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.Review;
import org.example.dto.request.CreateReviewByCurrentUserRequest;
import org.example.dto.request.UpdateReviewRequest;
import org.example.dto.response.ReviewResponse;
import org.example.mapper.ReviewMapper;
import org.example.service.CurrentUserService;
import org.example.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<ReviewResponse> findAllReviews() {
        List<Review> reviews = reviewService.findAllReviews();
        return reviewMapper.toResponseList(reviews);
    }

    @GetMapping("/{reviewId}")
    public ReviewResponse findReviewWithDetails(@PathVariable Long reviewId) {
        Review review = reviewService.findReviewWithDetails(reviewId);
        return reviewMapper.toResponse(review);
    }

    @PostMapping
    public ReviewResponse createReview(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateReviewByCurrentUserRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        Review review = reviewService.createReviewForCurrentUser(currentUser, request);
        return reviewMapper.toResponse(review);
    }

    @GetMapping("/order/{orderId}")
    public List<ReviewResponse> findReviewsByOrder(@PathVariable Long orderId) {
        List<Review> reviews = reviewService.findReviewsByOrder(orderId);
        return reviewMapper.toResponseList(reviews);
    }

    @GetMapping("/reviewer/{reviewerId}")
    public List<ReviewResponse> findReviewsByReviewer(@PathVariable Long reviewerId) {
        List<Review> reviews = reviewService.findReviewsByReviewer(reviewerId);
        return reviewMapper.toResponseList(reviews);
    }

    @GetMapping("/target-user/{targetUserId}")
    public List<ReviewResponse> findReviewsByTargetUser(@PathVariable Long targetUserId) {
        List<Review> reviews = reviewService.findReviewsByTargetUser(targetUserId);
        return reviewMapper.toResponseList(reviews);
    }

    @GetMapping("/seller/{sellerId}")
    public List<ReviewResponse> findSellerReviews(@PathVariable Long sellerId) {
        List<Review> reviews = reviewService.findSellerReviewsSorted(sellerId);
        return reviewMapper.toResponseList(reviews);
    }

    @PatchMapping("/{reviewId}")
    public void updateReview(
            HttpServletRequest httpRequest,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        reviewService.updateReviewForCurrentUser(
                currentUser,
                reviewId,
                request.rating(),
                request.comment()
        );
    }
}