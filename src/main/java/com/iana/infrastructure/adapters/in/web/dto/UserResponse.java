package com.iana.infrastructure.adapters.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iana.domain.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String role;
    private boolean active;
    private boolean emailVerified;
    private String verificationToken;
    private String verificationLink;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(UUID id, String name, String email, String role, boolean active, LocalDateTime createdAt) {
        this(id, name, email, role, active, active, null, createdAt);
    }

    public UserResponse(UUID id, String name, String email, String role, boolean active, boolean emailVerified, String verificationToken, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.emailVerified = emailVerified;
        this.verificationToken = verificationToken;
        String baseUrl = System.getenv("RENDER") != null ? "https://iana-aag2.onrender.com" : "http://localhost:8080";
        this.verificationLink = verificationToken != null
                ? baseUrl + "/api/v1/auth/verify-email?token=" + verificationToken
                : null;
        this.createdAt = createdAt;
    }

    public static UserResponse fromDomain(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified(),
                user.getVerificationToken(),
                user.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getVerificationLink() {
        return verificationLink;
    }

    public void setVerificationLink(String verificationLink) {
        this.verificationLink = verificationLink;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


