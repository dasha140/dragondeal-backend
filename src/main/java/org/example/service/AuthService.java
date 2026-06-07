package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.EmailVerification;
import org.example.domain.enums.UserRole;
import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.response.AuthResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AppUserRepository;
import org.example.repository.EmailVerificationRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.dto.request.RegisterConfirmRequest;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import org.example.dto.request.RequestEmailChangeRequest;
import org.example.dto.request.ConfirmEmailChangeRequest;
import org.example.dto.request.PasswordResetRequest;
import org.example.dto.request.PasswordResetConfirmRequest;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public Optional<String> requestRegistrationCode(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        String password = request.password();

        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        emailVerificationRepository.deleteByEmailIgnoreCase(email);

        String code = generateCode();

        EmailVerification verification = EmailVerification.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        emailVerificationRepository.save(verification);

        return emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public AuthResponse confirmRegistration(RegisterConfirmRequest request) {
        String email = request.email().trim().toLowerCase();
        String code = request.code().trim();

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResourceNotFoundException("Verification code not found"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.deleteByEmailIgnoreCase(email);
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!verification.getCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (appUserRepository.existsByUsernameIgnoreCase(verification.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (appUserRepository.existsByEmailIgnoreCase(verification.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        AppUser user = AppUser.builder()
                .displayName(verification.getUsername())
                .username(verification.getUsername())
                .email(verification.getEmail())
                .passwordHash(verification.getPasswordHash())
                .avatarUrl("")
                .rating(BigDecimal.ZERO)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();

        AppUser savedUser = appUserRepository.save(user);

        emailVerificationRepository.deleteByEmailIgnoreCase(email);

        return toAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username) {
        return appUserRepository.existsByUsernameIgnoreCase(username.trim());
    }

    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return appUserRepository.existsByEmailIgnoreCase(email.trim().toLowerCase());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsernameIgnoreCase(request.username().trim())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (appUserRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        AppUser user = AppUser.builder()
                .displayName(request.username())
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .avatarUrl("")
                .rating(BigDecimal.ZERO)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();

        AppUser savedUser = appUserRepository.save(user);

        return toAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse me(AppUser user) {
        return toAuthResponse(user);
    }

    @Transactional
    public Optional<String> requestEmailChange(AppUser currentUser, RequestEmailChangeRequest request) {
        String newEmail = request.newEmail().trim().toLowerCase();
        String currentPassword = request.currentPassword();

        AppUser user = appUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid current password");
        }

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("New email must be different");
        }

        if (appUserRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        emailVerificationRepository.deleteByEmailIgnoreCase(newEmail);

        String code = generateCode();

        EmailVerification verification = EmailVerification.builder()
                .username(user.getUsername())
                .email(newEmail)
                .passwordHash(user.getPasswordHash())
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        emailVerificationRepository.save(verification);

        return emailService.sendVerificationCode(newEmail, code);
    }

    @Transactional
    public AuthResponse confirmEmailChange(AppUser currentUser, ConfirmEmailChangeRequest request) {
        String newEmail = request.newEmail().trim().toLowerCase();
        String code = request.code().trim();

        AppUser user = appUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailIgnoreCaseOrderByCreatedAtDesc(newEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Verification code not found"));

        if (!verification.getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("Verification code does not belong to this user");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.deleteByEmailIgnoreCase(newEmail);
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!verification.getCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (appUserRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        user.setEmail(newEmail);

        AppUser savedUser = appUserRepository.save(user);

        emailVerificationRepository.deleteByEmailIgnoreCase(newEmail);

        return toAuthResponse(savedUser);
    }

    @Transactional
    public Optional<String> requestPasswordReset(PasswordResetRequest request) {
        String email = request.email().trim().toLowerCase();

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by email"));

        emailVerificationRepository.deleteByEmailIgnoreCase(email);

        String code = generateCode();

        EmailVerification verification = EmailVerification.builder()
                .username(user.getUsername())
                .email(email)
                .passwordHash(user.getPasswordHash())
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        emailVerificationRepository.save(verification);

        return emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public AuthResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        String email = request.email().trim().toLowerCase();
        String code = request.code().trim();
        String newPassword = request.newPassword();

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by email"));

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResourceNotFoundException("Verification code not found"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.deleteByEmailIgnoreCase(email);
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!verification.getCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        String newPasswordHash = passwordEncoder.encode(newPassword);

        user.setPasswordHash(newPasswordHash);

        AppUser savedUser = appUserRepository.save(user);

        emailVerificationRepository.deleteByEmailIgnoreCase(email);

        return toAuthResponse(savedUser);
    }

    private String generateCode() {
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        String token = jwtService.generateToken(user);

        String displayName = user.getDisplayName();

        if (displayName == null || displayName.isBlank()) {
            displayName = user.getUsername();
        }

        return new AuthResponse(
                user.getId(),
                displayName,
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRating(),
                user.getRole().name(),
                user.getCreatedAt(),
                token
        );
    }
}