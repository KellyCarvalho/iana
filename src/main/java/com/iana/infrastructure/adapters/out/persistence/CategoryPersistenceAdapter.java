package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Category;
import com.iana.domain.ports.out.CategoryRepositoryPort;
import com.iana.infrastructure.adapters.out.persistence.entity.CategoryJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.CategoryPersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    private final SpringDataCategoryRepository repository;
    private final CategoryPersistenceMapper mapper;

    public CategoryPersistenceAdapter(SpringDataCategoryRepository repository, CategoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Category save(Category category) {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        if (currentUser != null && (category.getOwnerEmail() == null || category.getOwnerEmail().isBlank())) {
            category.setOwnerEmail(currentUser);
        }
        CategoryJpaEntity entity = mapper.toEntity(category);
        if (currentUser != null && (entity.getOwnerEmail() == null || entity.getOwnerEmail().isBlank())) {
            entity.setOwnerEmail(currentUser);
        }
        CategoryJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        if (currentUser != null) {
            return repository.findByIdAndOwnerEmail(id, currentUser).map(mapper::toDomain);
        }
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        List<CategoryJpaEntity> entities = (currentUser != null)
                ? repository.findByOwnerEmail(currentUser)
                : repository.findAll();
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        if (currentUser != null) {
            repository.findByIdAndOwnerEmail(id, currentUser)
                    .ifPresent(entity -> repository.deleteById(entity.getId()));
            return;
        }
        repository.deleteById(id);
    }
}
