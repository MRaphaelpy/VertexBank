package com.mraphaelpy.vertexbank.userservice.controller;

import com.mraphaelpy.vertexbank.userservice.dtos.CreateUserProfileRequest;
import com.mraphaelpy.vertexbank.userservice.dtos.UpdateUserProfileRequest;
import com.mraphaelpy.vertexbank.userservice.dtos.UserProfileResponse;
import com.mraphaelpy.vertexbank.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(
            @RequestBody @Valid CreateUserProfileRequest request,
            @RequestAttribute("userId") UUID userId) {
        UserProfileResponse response = userProfileService.createUser(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @RequestAttribute("userId") UUID userId) {
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfileById(
            @PathVariable("userId") UUID userId) {
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @RequestBody @Valid UpdateUserProfileRequest request,
            @RequestAttribute("userId") UUID userId) {
        UserProfileResponse response = userProfileService.updateUserProfile(request, userId);
        return ResponseEntity.ok(response);
    }
}
