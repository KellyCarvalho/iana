package com.iana.infrastructure.adapters.out.persistence.entity;

// Substituída por LinkJpaEntity para desacoplamento total de microsserviço
public class LinkContentEntity {
}


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String url;

    private String description;

    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookJpaEntity book;

    public LinkContentEntity() {
    }

    public LinkContentEntity(UUID id, String url, String description, boolean active, LocalDateTime createdAt, BookJpaEntity book) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.book = book;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public BookJpaEntity getBook() {
        return book;
    }

    public void setBook(BookJpaEntity book) {
        this.book = book;
    }
}

