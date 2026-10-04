package com.iana.infrastructure.adapters.in.web.dto;

import com.iana.domain.model.Book;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookResponse {

    private UUID id;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private UUID categoryId;
    private LocalDateTime createdAt;

    public BookResponse() {
    }

    public static BookResponse fromDomain(Book book) {
        if (book == null) return null;
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setDescription(book.getDescription());
        response.setCoverUrl(book.getCoverUrl());
        response.setCategoryId(book.getCategoryId());
        response.setCreatedAt(book.getCreatedAt());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
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

