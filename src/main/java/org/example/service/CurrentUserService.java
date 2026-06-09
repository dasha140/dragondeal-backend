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
        String token = extractToken(request);

        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Authorization token is required");
        }

        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        Long userId = jwtService.extractUserId(token);

        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    /**
     * Extract JWT from request, trying multiple header sources.
     * Order of precedence:
     *   1. Authorization: Bearer <token>  (standard, works on direct backend)
     *   2. X-Auth-Token: <token>           (works through Yandex Cloud Functions proxy)
     *   3. X-Auth-Token: Bearer <token>    (same, with Bearer prefix stripped)
     */
    private String extractToken(HttpServletRequest request) {
        // Try standard Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                return token;
            }
        }

        // Fallback: try X-Auth-Token header (for Yandex Cloud Functions proxy)
        String xAuthToken = request.getHeader("X-Auth-Token");
        if (xAuthToken != null && !xAuthToken.isEmpty()) {
            // Strip "Bearer " prefix if present
            if (xAuthToken.startsWith("Bearer ")) {
                return xAuthToken.substring(7).trim();
            }
            return xAuthToken.trim();
        }

        return null;
    }
}