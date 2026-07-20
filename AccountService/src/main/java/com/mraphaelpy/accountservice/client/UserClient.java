package com.mraphaelpy.accountservice.client;

import com.mraphaelpy.accountservice.config.FeignConfig;
import com.mraphaelpy.accountservice.dtos.UserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8082}", configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/users/{userId}")
    UserProfileDTO getUserById(@PathVariable("userId") UUID userId);
}
