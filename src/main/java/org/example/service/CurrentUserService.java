package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    public AppUser getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization token is required");
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        Long userId = jwtService.extractUserId(token);

        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}