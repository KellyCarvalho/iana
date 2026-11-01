package com.iana.infrastructure.adapters.in.web.dto;

import com.iana.domain.model.Category;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CategoryResponse {

    private UUID id;
    private String name;
    private String description;
    private List<LinkResponse> links = Collections.emptyList();

    public CategoryResponse() {
    }

    public static CategoryResponse fromDomain(Category category) {
        return fromDomain(category, Collections.emptyList());
    }

    public static CategoryResponse fromDomain(Category category, List<LinkResponse> links) {
        if (category == null) return null;
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setLinks(links != null ? links : Collections.emptyList());
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

    public List<LinkResponse> getLinks() {
        return links;
    }

    public void setLinks(List<LinkResponse> links) {
        this.links = links;
    }
}

