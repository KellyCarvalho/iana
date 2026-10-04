package com.iana.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_links")
public class LinkJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String url;

    private String title;

    private String description;

    private UUID bookId;

    private UUID categoryId;

    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LinkJpaEntity() {
    }

    public LinkJpaEntity(UUID id, String url, String title, String description, UUID bookId, UUID categoryId, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.description = description;
        this.bookId = bookId;
        this.categoryId = categoryId;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
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
