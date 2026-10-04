package com.iana.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateLinkRequest {

    @NotBlank(message = "A URL é obrigatória")
    private String url;

    @Size(max = 150, message = "O título não pode exceder 150 caracteres")
    private String title;

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres")
    private String description;

    private UUID bookId;
    private UUID categoryId;

    public CreateLinkRequest() {
    }


    public CreateLinkRequest(String url, String title, String description, UUID bookId, UUID categoryId) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.bookId = bookId;
        this.categoryId = categoryId;
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
}
