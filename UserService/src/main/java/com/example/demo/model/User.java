package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @NotBlank(message = "Name cannot be blank")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password; // This should be hashed before storing

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Contact number must be valid")
    @Column(nullable = true) // Made explicitly nullable since it's not marked with @NotBlank
    private String contactNumber;

    @NotBlank(message = "Roles cannot be blank")
    @Pattern(regexp = "^(user|organizer|admin)$", message = "Role must be either 'user', 'organizer', or 'admin'")
    @Column(nullable = false)
    private String roles;
}

