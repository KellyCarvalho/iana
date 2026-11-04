package com.iana.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateExpenseRequest {

    @NotBlank(message = "O nome da despesa é obrigatório")
    @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
    private String name;

    @NotNull(message = "O valor da despesa é obrigatório")
    @Positive(message = "O valor da despesa deve ser maior que zero")
    private BigDecimal amount;

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres")
    private String description;

    private UUID categoryId;

    public CreateExpenseRequest() {
    }

    public CreateExpenseRequest(String name, BigDecimal amount, String description, UUID categoryId) {
        this.name = name;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
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
}
