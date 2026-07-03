package com.lumora.service;

import com.lumora.dto.*;
import com.lumora.model.Role;
import com.lumora.model.User;
import com.lumora.model.UserProfile;
import com.lumora.repository.UserProfileRepository;
import com.lumora.repository.UserRepository;
import com.lumora.util.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .build();

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .bio("Welcome to my Lumora profile!")
                .user(savedUser)
                .build();
        userProfileRepository.save(profile);
        savedUser.setProfile(profile);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUserResponse() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toList()))
                .build();
    }

    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        userProfileRepository.save(profile);

        return UserProfileResponse.builder()
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
