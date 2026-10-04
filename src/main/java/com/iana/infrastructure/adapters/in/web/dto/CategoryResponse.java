package com.iana.infrastructure.adapters.in.web.dto;

import com.iana.domain.model.Category;

import java.util.UUID;

public class CategoryResponse {

    private UUID id;
    private String name;
    private String description;

    public CategoryResponse() {
    }

    public static CategoryResponse fromDomain(Category category) {
        if (category == null) return null;
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
