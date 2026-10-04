package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.LinkJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLinkRepository extends JpaRepository<LinkJpaEntity, UUID> {
    List<LinkJpaEntity> findByBookId(UUID bookId);
    List<LinkJpaEntity> findByCategoryId(UUID categoryId);
}
