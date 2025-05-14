package com.example.demo.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import com.example.demo.util.JwtUtil;
import com.google.common.net.HttpHeaders;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil util;

    public static class Config {
    }

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return handleUnauthorized(exchange.getResponse(), "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return handleUnauthorized(exchange.getResponse(), "Invalid authorization header", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7); // Extract token after "Bearer "
                try {
                    String role = util.extractRolesFromToken(token);
                    String requestedPath = exchange.getRequest().getPath().toString();

                    if (!isAuthorized(role, requestedPath)) {
                        return handleUnauthorized(exchange.getResponse(), "Unauthorized access", HttpStatus.FORBIDDEN);
                    }

                } catch (Exception e) {
                    return handleUnauthorized(exchange.getResponse(), "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    //there are 3 roles: ADMIN, ORGANIZER, USER
    // ADMIN can access all endpoints
    // ORGANIZER can access update user, create event, update event, delete event,get feedback by event, get average rating by event, get all tickets by event id, get all feedbacks by event id, get all tickets by user id
    // USER can access update user, get all events, get event by id, get all tickets, get ticket by id, get all feedbacks, get feedback by id, get all notifications, get notification by id

    private boolean isAuthorized(String role, String path) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return path.startsWith("/user") || path.startsWith("/event")
            || path.startsWith("/ticket") || path.startsWith("/feedback") 
            || path.startsWith("/notification");
        } 
        else if ("ORGANIZER".equalsIgnoreCase(role)) {
            return path.startsWith("/user/update") || path.startsWith("/user/getUserById") 
          
            || path.startsWith("/event/create")  || path.startsWith("/event/update") 
            || path.startsWith("/event/delete") || path.startsWith("/event/filterByCategory") 
            || path.startsWith("/event/getAllEvents") || path.startsWith("/event/getEventById") 
            || path.startsWith("/event/organizer")  || path.startsWith(" /event/search")
            || path.startsWith("/event/filterByLocation") 
            
            || path.startsWith("/feedback/getByFeedbackId") || path.startsWith("/feedback/getAllFeedbacksByUser") 
            || path.startsWith("/feedback/getAllFeedbacksByEvent") || path.startsWith("/feedback/getAverageRatingByEvent") 
            
            || path.startsWith("/notification/getAllNotificationsByEventId")

            
            || path.startsWith("/ticket/getTicketByEventId") ;

        }
        else if ("USER".equalsIgnoreCase(role)) {
            return path.startsWith("/user/update") || path.startsWith("/user/getUserById") 
            || path.startsWith("/event/getAllEvents")  || path.startsWith("/event/search")
            || path.startsWith("/event/getEventById")   || path.startsWith("/event/filterByCategory") 
            || path.startsWith("/event/filterByLocation") 
            
            || path.startsWith("/ticket/book")  || path.startsWith("/ticket/cancel")
            || path.startsWith("/ticket/getTicketById") || path.startsWith("/ticket/getTicketByUserId") 
        
            || path.startsWith("/event/filterByLocation") 
            || path.startsWith("/event/getTicketByUserId") || path.startsWith("/ticket/cancel") 
           
            || path.startsWith("/feedback/save") || path.startsWith("/feedback/update")
            || path.startsWith("/feedback/delete") || path.startsWith("/feedback/getByFeedbackId")
            || path.startsWith("/feedback/getAllFeedbacksByUser") || path.startsWith("/feedback/getAllFeedbacksByEvent") 
            || path.startsWith("/feedback/getAverageRatingByEvent")
           
            || path.startsWith("/notification/getAllNotificationsByUserId");
        }

        return false;
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        String responseBody = String.format("{\"error\": \"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody.getBytes())));
    }
}

