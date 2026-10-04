package com.iana.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class LinkCreatedEvent {

    private UUID linkId;
    private String url;
    private UUID bookId;
    private UUID categoryId;
    private boolean active;
    private LocalDateTime timestamp;

    public LinkCreatedEvent() {
    }

    public LinkCreatedEvent(UUID linkId, String url, UUID bookId, UUID categoryId, boolean active) {
        this.linkId = linkId;
        this.url = url;
        this.bookId = bookId;
        this.categoryId = categoryId;
        this.active = active;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getLinkId() {
        return linkId;
    }

    public void setLinkId(UUID linkId) {
        this.linkId = linkId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
