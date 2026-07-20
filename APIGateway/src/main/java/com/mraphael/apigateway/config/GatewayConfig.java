package com.mraphael.apigateway.config;

import com.mraphael.apigateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;

    @Value("${services.auth-url}")
    private String authUrl;

    @Value("${services.user-url}")
    private String userUrl;

    @Value("${services.account-url}")
    private String accountUrl;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> authRoutes() {
        return route("auth-service")
                .route(path("/auth/**"), http())
                .before(BeforeFilterFunctions.uri(URI.create(authUrl)))
                .filter(authenticationFilter.authenticate())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userRoutes() {
        return route("user-service")
                .route(path("/users/**"), http())
                .before(BeforeFilterFunctions.uri(URI.create(userUrl)))
                .filter(authenticationFilter.authenticate())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> accountRoutes() {
        return route("account-service")
                .route(path("/accounts/**"), http())
                .before(BeforeFilterFunctions.uri(URI.create(accountUrl)))
                .filter(authenticationFilter.authenticate())
                .build();
    }
}
