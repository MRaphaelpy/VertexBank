package com.mraphaelpy.vertexbank.authservice.repository;

import com.mraphaelpy.vertexbank.authservice.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {
}
