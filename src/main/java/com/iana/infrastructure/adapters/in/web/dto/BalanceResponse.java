package com.iana.infrastructure.adapters.in.web.dto;

import com.iana.domain.model.Balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BalanceResponse {

    private UUID id;
    private String source;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public BalanceResponse() {
    }

    public static BalanceResponse fromDomain(Balance balance) {
        if (balance == null) return null;
        BalanceResponse response = new BalanceResponse();
        response.setId(balance.getId());
        response.setSource(balance.getSource());
        response.setAmount(balance.getAmount());
        response.setDescription(balance.getDescription());
        response.setCreatedAt(balance.getCreatedAt());
        return response;
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
}
