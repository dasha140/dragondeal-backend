package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.enums.UserRole;
import org.example.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;

    @Transactional
    public AppUser updatePersonalData(Long userId, String displayName, String email) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String cleanDisplayName = displayName == null ? "" : displayName.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();

        if (cleanDisplayName.isEmpty()) {
            throw new IllegalArgumentException("Display name is required");
        }

        if (cleanDisplayName.length() > 80) {
            throw new IllegalArgumentException("Display name must be at most 80 characters");
        }

        if (cleanEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!cleanEmail.equalsIgnoreCase(user.getEmail()) && appUserRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        user.setDisplayName(cleanDisplayName);
        user.setEmail(cleanEmail);

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser updateMyProfile(AppUser currentUser, String displayName, String avatarUrl) {
        AppUser user = appUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUser.getId()));

        if (displayName != null) {
            String cleanDisplayName = displayName.trim();

            if (cleanDisplayName.isEmpty()) {
                throw new IllegalArgumentException("Display name is required");
            }

            if (cleanDisplayName.length() > 80) {
                throw new IllegalArgumentException("Display name is too long");
            }

            user.setDisplayName(cleanDisplayName);
        }

        if (avatarUrl != null) {
            String cleanAvatarUrl = avatarUrl.trim();

            if (!cleanAvatarUrl.isEmpty()) {
                user.setAvatarUrl(cleanAvatarUrl);
            }
        }

        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAllUsers() {
        return appUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser findUserWithWallets(Long userId) {
        return appUserRepository.findWithWalletsById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Transactional(readOnly = true)
    public AppUser findByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by email: " + email));
    }

    @Transactional(readOnly = true)
    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found by username: " + username));
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return appUserRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return appUserRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<AppUser> findUsersByRole(UserRole role) {
        return appUserRepository.findByRole(role);
    }

    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new IllegalArgumentException("Avatar url is required");
        }

        int updatedRows = appUserRepository.updateAvatarById(
                userId,
                avatarUrl
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    @Transactional
    public void updatePasswordHash(Long userId, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }

        int updatedRows = appUserRepository.updatePasswordHashById(
                userId,
                passwordHash
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    @Transactional
    public void updateRating(Long userId, BigDecimal rating) {
        if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        int updatedRows = appUserRepository.updateRatingById(
                userId,
                rating
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    @Transactional
    public void makeAdmin(Long userId) {
        int updatedRows = appUserRepository.updateRoleById(
                userId,
                UserRole.ADMIN
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    @Transactional
    public void makeUser(Long userId) {
        int updatedRows = appUserRepository.updateRoleById(
                userId,
                UserRole.USER
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }
}