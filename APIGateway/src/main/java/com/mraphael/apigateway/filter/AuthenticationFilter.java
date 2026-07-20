package com.mraphael.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

@Component
public class AuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/signup",
            "/auth/login",
            "/auth/refresh"
    );

    @Value("${services.auth-url}")
    private String authServiceUrl;

    public HandlerFilterFunction<ServerResponse, ServerResponse> authenticate() {
        return (request, next) -> {
            String path = request.path();

            if (isPublic(path)) {
                return next.handle(request);
            }

            String authHeader = request.headers().firstHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"status\":401,\"title\":\"Unauthorized\",\"detail\":\"Token ausente.\"}");
            }

            String token = authHeader.substring(7);

            try {
                RestClient restClient = RestClient.create();
                restClient.get()
                        .uri(authServiceUrl + "/auth/validate?token=" + token)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                log.warn("Token inválido ou Auth Service indisponível: {}", e.getMessage());
                return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"status\":401,\"title\":\"Unauthorized\",\"detail\":\"Token inválido ou expirado.\"}");
            }

            return next.handle(request);
        };
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
