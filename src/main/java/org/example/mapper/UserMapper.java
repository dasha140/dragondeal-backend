package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.enums.OrderStatus;
import org.example.dto.response.UserResponse;
import org.example.repository.AppOrderRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final AppOrderRepository appOrderRepository;

    public UserResponse toResponse(AppUser user) {
        Long completedOrdersCount = appOrderRepository.countBySeller_IdAndStatus(
                user.getId(),
                OrderStatus.COMPLETED
        );

        BigDecimal rating = user.getRating();

        if (rating == null) {
            rating = BigDecimal.ZERO;
        }

        return new UserResponse(
                user.getId(),
                getDisplayName(user),
                user.getUsername(),
                user.getAvatarUrl(),
                rating,
                completedOrdersCount,
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public List<UserResponse> toResponseList(List<AppUser> users) {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    private String getDisplayName(AppUser user) {
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            return user.getUsername();
        }

        return user.getDisplayName();
    }
}