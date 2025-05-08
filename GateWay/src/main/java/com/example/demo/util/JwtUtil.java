package com.example.demo.util;

import java.security.Key;
import java.util.List;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtil {

    public static final String SECRET = "d3780ec3d1cfaba271e0538d4fae686d8367e10155ee424691fbf191eabec53d";

    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    public String extractRolesFromToken(final String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        System.out.println("Step2: " + claims);

        // Safely extract roles from the claims
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesClaim;
            System.out.println("Step3: " + roles);
            // Return the first role or concatenate roles as needed
            return String.join(",", roles);
        } else if (rolesClaim instanceof String) {
            System.out.println("Step3: " + rolesClaim);
            return (String) rolesClaim;
        } else {
            throw new IllegalArgumentException("Invalid roles format in token");
        }
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

