package com.iana.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCategoryRequest {

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(max = 100, message = "O nome não pode exceder 100 caracteres")
    private String name;

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres")
    private String description;

    public CreateCategoryRequest() {
    }


    public CreateCategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
