package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppOrder;
import org.example.domain.AppUser;
import org.example.domain.Review;
import org.example.domain.enums.OrderStatus;
import org.example.dto.request.CreateReviewByCurrentUserRequest;
import org.example.dto.request.CreateReviewRequest;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AppOrderRepository;
import org.example.repository.AppUserRepository;
import org.example.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppOrderRepository appOrderRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Review findReviewWithDetails(Long reviewId) {
        return reviewRepository.findWithDetailsById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
    }

    @Transactional(readOnly = true)
    public List<Review> findReviewsByOrder(Long orderId) {
        return reviewRepository.findByOrder_Id(orderId);
    }

    @Transactional(readOnly = true)
    public List<Review> findReviewsByReviewer(Long reviewerId) {
        return reviewRepository.findByReviewer_Id(reviewerId);
    }

    @Transactional(readOnly = true)
    public List<Review> findReviewsByTargetUser(Long targetUserId) {
        return reviewRepository.findByTargetUser_Id(targetUserId);
    }

    @Transactional(readOnly = true)
    public List<Review> findSellerReviewsSorted(Long sellerId) {
        return reviewRepository.findByTargetUser_IdOrderByCreatedAtDesc(sellerId);
    }

    @Transactional
    public Review createReviewForCurrentUser(
            AppUser currentUser,
            CreateReviewByCurrentUserRequest request
    ) {
        AppOrder order = appOrderRepository.findWithDetailsById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.orderId()));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Оценку можно оставить только после завершения заказа");
        }

        if (order.getBuyer() == null || !order.getBuyer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Оценить продавца может только покупатель этого заказа");
        }

        if (order.getSeller() == null) {
            throw new IllegalArgumentException("Seller not found for order: " + order.getId());
        }

        if (reviewRepository.existsByOrder_IdAndReviewer_Id(order.getId(), currentUser.getId())) {
            throw new IllegalArgumentException("Вы уже оставили отзыв по этому заказу");
        }

        validateRating(request.rating());

        String cleanComment = "";

        if (request.comment() != null) {
            cleanComment = request.comment().trim();
        }

        Review review = Review.builder()
                .order(order)
                .reviewer(currentUser)
                .targetUser(order.getSeller())
                .rating(request.rating())
                .comment(cleanComment)
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        updateUserAverageRating(order.getSeller().getId());

        return savedReview;
    }

    @Transactional
    public Review createReview(CreateReviewRequest request) {
        AppOrder order = appOrderRepository.findWithDetailsById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.orderId()));

        AppUser reviewer = appUserRepository.findById(request.reviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found: " + request.reviewerId()));

        AppUser targetUser = appUserRepository.findById(request.targetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found: " + request.targetUserId()));

        validateRating(request.rating());

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Review can be created only for completed order");
        }

        boolean reviewerIsBuyer = order.getBuyer().getId().equals(reviewer.getId());
        boolean reviewerIsSeller = order.getSeller().getId().equals(reviewer.getId());

        if (!reviewerIsBuyer && !reviewerIsSeller) {
            throw new IllegalArgumentException("Reviewer is not a participant of this order");
        }

        boolean targetIsBuyer = order.getBuyer().getId().equals(targetUser.getId());
        boolean targetIsSeller = order.getSeller().getId().equals(targetUser.getId());

        if (!targetIsBuyer && !targetIsSeller) {
            throw new IllegalArgumentException("Target user is not a participant of this order");
        }

        if (reviewer.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("User cannot review themselves");
        }

        if (reviewRepository.existsByOrder_IdAndReviewer_Id(order.getId(), reviewer.getId())) {
            throw new IllegalArgumentException("Reviewer already reviewed this order");
        }

        Review review = Review.builder()
                .order(order)
                .reviewer(reviewer)
                .targetUser(targetUser)
                .rating(request.rating())
                .comment(request.comment() != null ? request.comment().trim() : "")
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        updateUserAverageRating(targetUser.getId());

        return savedReview;
    }

    @Transactional
    public void updateReview(Long reviewId, Integer rating, String comment) {
        validateRating(rating);

        Review review = reviewRepository.findWithDetailsById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");

        Review savedReview = reviewRepository.save(review);

        updateUserAverageRating(savedReview.getTargetUser().getId());
    }

    @Transactional
    public void updateReviewForCurrentUser(
            AppUser currentUser,
            Long reviewId,
            Integer rating,
            String comment
    ) {
        validateRating(rating);

        Review review = reviewRepository.findWithDetailsById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!review.getReviewer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only review author can update this review");
        }

        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");

        Review savedReview = reviewRepository.save(review);

        updateUserAverageRating(savedReview.getTargetUser().getId());
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private void updateUserAverageRating(Long targetUserId) {
        Double averageRating = reviewRepository.getAverageRatingByTargetUserId(targetUserId);

        if (averageRating == null) {
            averageRating = 0.0;
        }

        BigDecimal rating = BigDecimal.valueOf(averageRating)
                .setScale(2, RoundingMode.HALF_UP);

        appUserRepository.updateRatingById(targetUserId, rating);
    }
}