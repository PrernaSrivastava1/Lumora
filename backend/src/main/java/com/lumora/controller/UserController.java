package com.lumora.controller;

import com.lumora.dto.*;
import com.lumora.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser() {
        AuthResponse userResponse = userService.getCurrentUserResponse();
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .data(userResponse)
                .build());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@RequestBody UpdateProfileRequest request) {
        UserProfileResponse profileResponse = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(profileResponse)
                .build());
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Password changed successfully")
                .build());
    }
}
