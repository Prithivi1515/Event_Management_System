package com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dto.AuthRequest;
import com.entity.UserInfo;
import com.repository.UserInfoRepository;
import com.service.JwtService;
import com.service.UserService;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserInfoRepository repo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/welcome") // http://localhost:9090/auth/welcome
    public String welcome() {
        return "Welcome, this endpoint is not secure.";
    }

    @PostMapping("/new") // http://localhost:9090/auth/new
    public String addNewUser(@RequestBody UserInfo userInfo) {
        return service.addUser(userInfo);
    }

    @PostMapping("/authenticate") // http://localhost:9090/auth/authenticate
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            UserInfo userInfo = repo.findByName(authRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with username: " + authRequest.getUsername()));

            String roles = userInfo.getRoles();
            if (roles == null || roles.trim().isEmpty()) {
                throw new IllegalArgumentException("User roles are not defined for username: " + authRequest.getUsername());
            }

            return jwtService.generateToken(authRequest.getUsername(), roles);
        } else {
            throw new UsernameNotFoundException("Invalid user credentials!");
        }
    }

    @GetMapping("/getroles/{username}") // http://localhost:9090/auth/getroles/{username}
    public String getRoles(@PathVariable String username) {
        return service.getRoles(username);
    }
}
