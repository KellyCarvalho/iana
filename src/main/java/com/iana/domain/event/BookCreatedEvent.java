package com.iana.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookCreatedEvent {

    private UUID bookId;
    private String title;
    private String author;
    private UUID categoryId;
    private LocalDateTime timestamp;

    public BookCreatedEvent() {
    }

    public BookCreatedEvent(UUID bookId, String title, String author, UUID categoryId) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.categoryId = categoryId;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
