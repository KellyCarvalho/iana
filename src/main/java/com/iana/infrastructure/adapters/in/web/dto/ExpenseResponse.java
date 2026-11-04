package com.iana.infrastructure.adapters.in.web.dto;

import com.iana.domain.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExpenseResponse {

    private UUID id;
    private String name;
    private BigDecimal amount;
    private String description;
    private UUID categoryId;
    private LocalDateTime createdAt;

    public ExpenseResponse() {
    }

    public static ExpenseResponse fromDomain(Expense expense) {
        if (expense == null) return null;
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setName(expense.getName());
        response.setAmount(expense.getAmount());
        response.setDescription(expense.getDescription());
        response.setCategoryId(expense.getCategoryId());
        response.setCreatedAt(expense.getCreatedAt());
        return response;
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
}
