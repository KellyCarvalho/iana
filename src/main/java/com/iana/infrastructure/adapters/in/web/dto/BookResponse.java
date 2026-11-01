package com.iana.infrastructure.adapters.in.web.dto;

import com.iana.domain.model.Book;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BookResponse {

    private UUID id;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private String linkUrl;
    private UUID categoryId;
    private List<LinkResponse> links = Collections.emptyList();
    private LocalDateTime createdAt;

    public BookResponse() {
    }

    public static BookResponse fromDomain(Book book) {
        return fromDomain(book, Collections.emptyList());
    }

    public static BookResponse fromDomain(Book book, List<LinkResponse> links) {
        if (book == null) return null;
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setDescription(book.getDescription());
        response.setCoverUrl(book.getCoverUrl());
        List<LinkResponse> safeLinks = links != null ? links : Collections.emptyList();
        response.setLinks(safeLinks);
        String resolvedLink = (book.getCoverUrl() != null && !book.getCoverUrl().isBlank())
                ? book.getCoverUrl()
                : (!safeLinks.isEmpty() ? safeLinks.get(0).getUrl() : null);
        response.setLinkUrl(resolvedLink);
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

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public List<LinkResponse> getLinks() {
        return links;
    }

    public void setLinks(List<LinkResponse> links) {
        this.links = links;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


