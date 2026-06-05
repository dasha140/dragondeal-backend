package org.example.dto.response;

import org.example.domain.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String displayName,
        String username,
        String avatarUrl,
        BigDecimal rating,
        Long completedOrdersCount,
        UserRole role,
        LocalDateTime createdAt
) {
}