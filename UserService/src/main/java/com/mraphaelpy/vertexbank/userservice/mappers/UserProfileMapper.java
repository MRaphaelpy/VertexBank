package com.mraphaelpy.vertexbank.userservice.mappers;

import com.mraphaelpy.vertexbank.userservice.dtos.CreateUserProfileRequest;
import com.mraphaelpy.vertexbank.userservice.dtos.UpdateUserProfileRequest;
import com.mraphaelpy.vertexbank.userservice.dtos.UserProfileResponse;
import com.mraphaelpy.vertexbank.userservice.entity.UserProfile;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserProfileMapper {
    UserProfile toEntity(CreateUserProfileRequest request);
    
    UserProfileResponse toResponse(UserProfile user);
    
    void updateEntityFromRequest(UpdateUserProfileRequest request, @MappingTarget UserProfile entity);

    @AfterMapping
    default void linkPhones(@MappingTarget UserProfile userProfile) {
        if (userProfile.getPhones() != null) {
            userProfile.getPhones().forEach(phone -> phone.setUserProfile(userProfile));
        }
    }
}
