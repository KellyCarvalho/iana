package com.iana.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_expenses")
public class ExpenseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    private UUID categoryId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "owner_email")
    private String ownerEmail;

    public ExpenseJpaEntity() {
    }

    public ExpenseJpaEntity(UUID id, String name, BigDecimal amount, String description, UUID categoryId, LocalDateTime createdAt) {
        this(id, name, amount, description, categoryId, createdAt, null);
    }

    public ExpenseJpaEntity(UUID id, String name, BigDecimal amount, String description, UUID categoryId, LocalDateTime createdAt, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.ownerEmail = ownerEmail;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
