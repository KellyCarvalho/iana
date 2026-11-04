package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Expense;
import com.iana.domain.ports.out.ExpenseRepositoryPort;
import com.iana.infrastructure.adapters.out.persistence.entity.ExpenseJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.ExpensePersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataExpenseRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ExpensePersistenceAdapter implements ExpenseRepositoryPort {

    private final SpringDataExpenseRepository repository;
    private final ExpensePersistenceMapper mapper;

    public ExpensePersistenceAdapter(SpringDataExpenseRepository repository, ExpensePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Expense save(Expense expense) {
        ExpenseJpaEntity entity = mapper.toEntity(expense);
        ExpenseJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Expense> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Expense> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByCategoryId(UUID categoryId) {
        return repository.findByCategoryId(categoryId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
