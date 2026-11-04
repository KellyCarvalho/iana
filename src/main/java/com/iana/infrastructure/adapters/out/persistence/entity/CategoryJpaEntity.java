package com.iana.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "tb_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "owner_email"})
)
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail = "";

    public CategoryJpaEntity() {
    }

    public CategoryJpaEntity(UUID id, String name, String description) {
        this(id, name, description, "");
    }

    public CategoryJpaEntity(UUID id, String name, String description, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerEmail = ownerEmail != null ? ownerEmail : "";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail != null ? ownerEmail : "";
    }
}
