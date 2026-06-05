package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.response.AuthResponse;
import org.example.service.AuthService;
import org.example.service.CurrentUserService;
import org.springframework.web.bind.annotation.*;
import org.example.dto.response.MessageResponse;
import org.example.dto.request.RegisterConfirmRequest;
import org.example.dto.request.FirebaseLoginRequest;import org.example.dto.request.RequestEmailChangeRequest;
import org.example.dto.request.ConfirmEmailChangeRequest;import org.example.dto.request.PasswordResetRequest;
import org.example.dto.request.PasswordResetConfirmRequest;
import org.example.dto.response.CodeSentResponse;

import java.util.Optional;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @PostMapping("/firebase-login")
    public AuthResponse firebaseLogin(
            @Valid @RequestBody FirebaseLoginRequest request
    ) {
        return authService.loginWithFirebase(request.idToken());
    }

    @PostMapping("/register/request")
    public CodeSentResponse requestRegistrationCode(
            @Valid @RequestBody RegisterRequest request
    ) {
        Optional<String> devCode = authService.requestRegistrationCode(request);
        return devCode
                .map(CodeSentResponse::sentWithDevCode)
                .orElseGet(CodeSentResponse::sent);
    }

    @PostMapping("/register/confirm")
    public AuthResponse confirmRegistration(
            @Valid @RequestBody RegisterConfirmRequest request
    ) {
        return authService.confirmRegistration(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/password-reset/request")
    public CodeSentResponse requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        Optional<String> devCode = authService.requestPasswordReset(request);
        return devCode
                .map(CodeSentResponse::sentWithDevCode)
                .orElseGet(CodeSentResponse::sent);
    }

    @PostMapping("/password-reset/confirm")
    public AuthResponse confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        return authService.confirmPasswordReset(request);
    }

    @GetMapping("/check-username")
    public MessageResponse checkUsername(@RequestParam String username) {
        if (authService.isUsernameTaken(username)) {
            return new MessageResponse("TAKEN");
        }

        return new MessageResponse("AVAILABLE");
    }

    @GetMapping("/check-email")
    public MessageResponse checkEmail(@RequestParam String email) {
        if (authService.isEmailTaken(email)) {
            return new MessageResponse("TAKEN");
        }

        return new MessageResponse("AVAILABLE");
    }

    @GetMapping("/me")
    public AuthResponse me(HttpServletRequest request) {
        AppUser currentUser = currentUserService.getCurrentUser(request);
        return authService.me(currentUser);
    }

    @PostMapping("/email-change/request")
    public CodeSentResponse requestEmailChange(
            HttpServletRequest httpRequest,
            @Valid @RequestBody RequestEmailChangeRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        Optional<String> devCode = authService.requestEmailChange(currentUser, request);

        return devCode
                .map(CodeSentResponse::sentWithDevCode)
                .orElseGet(CodeSentResponse::sent);
    }

    @PostMapping("/email-change/confirm")
    public AuthResponse confirmEmailChange(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ConfirmEmailChangeRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        return authService.confirmEmailChange(currentUser, request);
    }
}