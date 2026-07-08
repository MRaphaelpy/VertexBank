package com.mraphaelpy.vertexbank.authservice.repository;

import com.mraphaelpy.vertexbank.authservice.entity.UserCredential;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.role"})
    Optional<UserCredential> findByEmail(String email);
    boolean existsByEmail(String email);
}
