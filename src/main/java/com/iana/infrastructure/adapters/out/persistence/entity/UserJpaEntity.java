package com.iana.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tb_users",
        uniqueConstraints = @UniqueConstraint(name = "uk_tb_users_email", columnNames = "email")
)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    private boolean active = true;

    private boolean emailVerified = false;

    private String verificationToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserJpaEntity() {
    }

    public UserJpaEntity(UUID id, String name, String email, String password, String role, boolean active, LocalDateTime createdAt) {
        this(id, name, email, password, role, active, active, null, createdAt);
    }

    public UserJpaEntity(UUID id, String name, String email, String password, String role, boolean active, boolean emailVerified, String verificationToken, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : "ROLE_USER";
        this.active = active;
        this.emailVerified = emailVerified;
        this.verificationToken = verificationToken;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

