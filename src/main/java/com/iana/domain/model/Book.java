package com.iana.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Book {

    private UUID id;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private UUID categoryId;
    private LocalDateTime createdAt;

    public Book() {
    }

    public Book(UUID id, String title, String author, String description, String coverUrl, UUID categoryId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverUrl = coverUrl;
        this.categoryId = categoryId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
