package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, UUID> {
}
