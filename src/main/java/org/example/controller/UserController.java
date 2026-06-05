package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.enums.UserRole;
import org.example.dto.request.UpdateUserAvatarRequest;
import org.example.dto.request.UpdateUserRatingRequest;
import org.example.dto.response.UserResponse;
import org.example.mapper.UserMapper;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;import jakarta.servlet.http.HttpServletRequest;
import org.example.dto.request.UpdateUserProfileRequest;
import org.example.service.CurrentUserService;import jakarta.servlet.http.HttpServletRequest;
import org.example.dto.request.UpdateUserPersonalDataRequest;
import org.example.dto.response.AuthResponse;
import org.example.service.AuthService;
import org.example.service.CurrentUserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final CurrentUserService currentUserService;
    private final AuthService authService;

    @GetMapping
    public List<UserResponse> findAllUsers() {
        List<AppUser> users = userService.findAllUsers();
        return userMapper.toResponseList(users);
    }

    @GetMapping("/{userId}")
    public UserResponse findUserWithWallets(@PathVariable Long userId) {
        AppUser user = userService.findUserWithWallets(userId);
        return userMapper.toResponse(user);
    }

    @GetMapping("/email/{email}")
    public UserResponse findByEmail(@PathVariable String email) {
        AppUser user = userService.findByEmail(email);
        return userMapper.toResponse(user);
    }

    @GetMapping("/username/{username}")
    public UserResponse findByUsername(@PathVariable String username) {
        AppUser user = userService.findByUsername(username);
        return userMapper.toResponse(user);
    }

    @GetMapping("/role/{role}")
    public List<UserResponse> findUsersByRole(@PathVariable UserRole role) {
        List<AppUser> users = userService.findUsersByRole(role);
        return userMapper.toResponseList(users);
    }

    @GetMapping("/email/{email}/exists")
    public boolean emailExists(@PathVariable String email) {
        return userService.emailExists(email);
    }

    @GetMapping("/username/{username}/exists")
    public boolean usernameExists(@PathVariable String username) {
        return userService.usernameExists(username);
    }

    @PatchMapping("/me/profile")
    public UserResponse updateMyProfile(
            HttpServletRequest httpRequest,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);

        AppUser updatedUser = userService.updateMyProfile(
                currentUser,
                request.displayName(),
                request.avatarUrl()
        );

        return userMapper.toResponse(updatedUser);
    }

    @PatchMapping("/{userId}/avatar")
    public void updateAvatar(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserAvatarRequest request
    ) {
        userService.updateAvatar(userId, request.avatarUrl());
    }

    @PatchMapping("/me/personal-data")
    public AuthResponse updateMyPersonalData(
            HttpServletRequest httpRequest,
            @Valid @RequestBody UpdateUserPersonalDataRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);

        AppUser updatedUser = userService.updatePersonalData(
                currentUser.getId(),
                request.displayName(),
                request.email()
        );

        return authService.me(updatedUser);
    }

    @PatchMapping("/{userId}/rating")
    public void updateRating(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRatingRequest request
    ) {
        userService.updateRating(userId, request.rating());
    }

    @PatchMapping("/{userId}/make-admin")
    public void makeAdmin(@PathVariable Long userId) {
        userService.makeAdmin(userId);
    }

    @PatchMapping("/{userId}/make-user")
    public void makeUser(@PathVariable Long userId) {
        userService.makeUser(userId);
    }
}