package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.ExpenseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataExpenseRepository extends JpaRepository<ExpenseJpaEntity, UUID> {
    List<ExpenseJpaEntity> findByCategoryId(UUID categoryId);
}
