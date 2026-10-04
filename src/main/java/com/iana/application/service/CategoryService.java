package com.iana.application.service;

import com.iana.domain.model.Category;
import com.iana.domain.ports.in.CreateCategoryUseCase;
import com.iana.domain.ports.in.FindCategoryUseCase;
import com.iana.domain.ports.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService implements CreateCategoryUseCase, FindCategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public CategoryService(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    public Category execute(Category category) {
        return categoryRepositoryPort.save(category);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryRepositoryPort.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepositoryPort.findAll();
    }
}
