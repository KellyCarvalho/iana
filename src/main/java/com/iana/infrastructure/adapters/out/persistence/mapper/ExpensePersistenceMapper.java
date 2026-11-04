package com.iana.infrastructure.adapters.out.persistence.mapper;

import com.iana.domain.model.Expense;
import com.iana.infrastructure.adapters.out.persistence.entity.ExpenseJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExpensePersistenceMapper {

    public ExpenseJpaEntity toEntity(Expense domain) {
        if (domain == null) return null;

        return new ExpenseJpaEntity(
                domain.getId(),
                domain.getName(),
                domain.getAmount(),
                domain.getDescription(),
                domain.getCategoryId(),
                domain.getCreatedAt()
        );
    }

    public Expense toDomain(ExpenseJpaEntity entity) {
        if (entity == null) return null;

        return new Expense(
                entity.getId(),
                entity.getName(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getCategoryId(),
                entity.getCreatedAt()
        );
    }
}
