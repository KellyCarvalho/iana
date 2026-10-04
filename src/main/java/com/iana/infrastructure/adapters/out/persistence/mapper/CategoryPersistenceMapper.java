package com.iana.infrastructure.adapters.out.persistence.mapper;

import com.iana.domain.model.Category;
import com.iana.infrastructure.adapters.out.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceMapper {

    public CategoryJpaEntity toEntity(Category domain) {
        if (domain == null) return null;
        return new CategoryJpaEntity(domain.getId(), domain.getName(), domain.getDescription());
    }

    public Category toDomain(CategoryJpaEntity entity) {
        if (entity == null) return null;
        return new Category(entity.getId(), entity.getName(), entity.getDescription());
    }
}
