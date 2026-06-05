package org.example.dto.response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record AuthResponse(
        Long id,
        String displayName,
        String username,
        String email,
        String avatarUrl,
        BigDecimal rating,
        String role,
        LocalDateTime createdAt,
        String accessToken ) { }