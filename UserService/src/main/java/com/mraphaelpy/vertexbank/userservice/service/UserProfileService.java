package com.mraphaelpy.vertexbank.userservice.service;

import com.mraphaelpy.vertexbank.userservice.dtos.CreateUserProfileRequest;
import com.mraphaelpy.vertexbank.userservice.dtos.UpdateUserProfileRequest;
import com.mraphaelpy.vertexbank.userservice.dtos.UserProfileResponse;
import com.mraphaelpy.vertexbank.userservice.entity.UserProfile;
import com.mraphaelpy.vertexbank.userservice.exception.CpfAlreadyExistsException;
import com.mraphaelpy.vertexbank.userservice.exception.UserProfileAlreadyExistsException;
import com.mraphaelpy.vertexbank.userservice.exception.UserProfileNotFoundException;
import com.mraphaelpy.vertexbank.userservice.mappers.UserProfileMapper;
import com.mraphaelpy.vertexbank.userservice.repository.UserProfileRepository;
import com.mraphaelpy.vertexbank.userservice.utils.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    @Transactional
    public UserProfileResponse createUser(CreateUserProfileRequest request, UUID userId) {
        if (userProfileRepository.existsByUserId(userId)) {
            throw new UserProfileAlreadyExistsException(ErrorMessages.USER_PROFILE_ALREADY_EXISTS);
        }
        if (userProfileRepository.existsByCpf(request.getCpf())) {
            throw new CpfAlreadyExistsException(ErrorMessages.CPF_ALREADY_EXISTS);
        }

        UserProfile userProfile = userProfileMapper.toEntity(request);
        userProfile.setUserId(userId);
        
        UserProfile saved = userProfileRepository.save(userProfile);
        return userProfileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId);
        if (userProfile == null) {
            throw new UserProfileNotFoundException(ErrorMessages.USER_PROFILE_NOT_FOUND);
        }
        return userProfileMapper.toResponse(userProfile);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(UpdateUserProfileRequest request, UUID userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId);
        if (userProfile == null) {
            throw new UserProfileNotFoundException(ErrorMessages.USER_PROFILE_NOT_FOUND);
        }

        userProfileMapper.updateEntityFromRequest(request, userProfile);
        UserProfile saved = userProfileRepository.save(userProfile);
        return userProfileMapper.toResponse(saved);
    }
}
