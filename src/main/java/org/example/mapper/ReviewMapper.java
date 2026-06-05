package org.example.mapper;

import org.example.domain.Review;
import org.example.dto.response.ReviewResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrder().getId(),
                review.getReviewer().getId(),
                review.getReviewer().getUsername(),
                review.getTargetUser().getId(),
                review.getTargetUser().getUsername(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream()
                .map(this::toResponse)
                .toList();
    }
}