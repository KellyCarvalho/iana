package com.iana.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Balance {

    private UUID id;
    private String source;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private String ownerEmail;

    public Balance() {
    }

    public Balance(UUID id, String source, BigDecimal amount, String description, LocalDateTime createdAt) {
        this(id, source, amount, description, createdAt, null);
    }

    public Balance(UUID id, String source, BigDecimal amount, String description, LocalDateTime createdAt, String ownerEmail) {
        this.id = id;
        this.source = source;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.ownerEmail = ownerEmail;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Balance balance = (Balance) o;
        return Objects.equals(id, balance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
