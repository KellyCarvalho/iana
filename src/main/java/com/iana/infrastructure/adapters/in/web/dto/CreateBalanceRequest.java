package com.iana.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateBalanceRequest {

    @NotBlank(message = "A origem do saldo é obrigatória")
    @Size(max = 150, message = "A origem não pode exceder 150 caracteres")
    private String source;

    @NotNull(message = "O valor do saldo é obrigatório")
    @Positive(message = "O valor do saldo deve ser maior que zero")
    private BigDecimal amount;

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres")
    private String description;

    public CreateBalanceRequest() {
    }

    public CreateBalanceRequest(String source, BigDecimal amount, String description) {
        this.source = source;
        this.amount = amount;
        this.description = description;
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
}
