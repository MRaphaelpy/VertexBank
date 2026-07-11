package com.mraphaelpy.vertexbank.userservice.repository;

import com.mraphaelpy.vertexbank.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    UserProfile findByUserId(UUID uuid);
    UserProfile findByCpf(String cpf);
    boolean existsByUserId(UUID uuid);
    boolean existsByCpf(String cpf);
}
