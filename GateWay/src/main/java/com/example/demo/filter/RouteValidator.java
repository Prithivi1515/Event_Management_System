package com.example.demo.filter;

import java.util.Arrays;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

    public static final String[] OPEN_API_ENDPOINTS = { "/auth/register", "/auth/new", "/auth/validate", "/eureka" ,"/event/getAllEvents","/event/filterByLocation","/event/filterByCategory","/event/search"};

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getPath().toString();
        return Arrays.stream(OPEN_API_ENDPOINTS)
                     .noneMatch(endpoint -> path.equals(endpoint)); // Match exact paths only
    };

}
