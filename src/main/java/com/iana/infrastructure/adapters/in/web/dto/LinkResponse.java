package com.iana.infrastructure.adapters.in.web.dto;

import com.iana.domain.model.Link;

import java.time.LocalDateTime;
import java.util.UUID;

public class LinkResponse {

    private UUID id;
    private String url;
    private String title;
    private String description;
    private UUID bookId;
    private UUID categoryId;
    private boolean active;
    private LocalDateTime createdAt;

    public LinkResponse() {
    }

    public static LinkResponse fromDomain(Link link) {
        if (link == null) return null;
        LinkResponse response = new LinkResponse();
        response.setId(link.getId());
        response.setUrl(link.getUrl());
        response.setTitle(link.getTitle());
        response.setDescription(link.getDescription());
        response.setBookId(link.getBookId());
        response.setCategoryId(link.getCategoryId());
        response.setActive(link.isActive());
        response.setCreatedAt(link.getCreatedAt());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
