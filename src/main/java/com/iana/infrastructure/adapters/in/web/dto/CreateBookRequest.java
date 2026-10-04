package com.iana.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateBookRequest {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 150, message = "O título não pode ter mais de 150 caracteres")
    private String title;

    @NotBlank(message = "O autor é obrigatório")
    @Size(max = 100, message = "O autor não pode ter mais de 100 caracteres")
    private String author;

    @Size(max = 1000, message = "A descrição não pode ter mais de 1000 caracteres")
    private String description;

    private String coverUrl;

    private UUID categoryId;

    public CreateBookRequest() {
    }


    public CreateBookRequest(String title, String author, String description, String coverUrl, UUID categoryId) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverUrl = coverUrl;
        this.categoryId = categoryId;
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
}

