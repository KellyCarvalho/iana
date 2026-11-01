package com.iana.application.service;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Category;
import com.iana.domain.ports.in.CreateCategoryUseCase;
import com.iana.domain.ports.in.DeleteCategoryUseCase;
import com.iana.domain.ports.in.FindCategoryUseCase;
import com.iana.domain.ports.in.UpdateCategoryUseCase;
import com.iana.domain.ports.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService implements CreateCategoryUseCase, FindCategoryUseCase, UpdateCategoryUseCase, DeleteCategoryUseCase {

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

    @Override
    public Category update(UUID id, Category category) {
        Category existing = categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + id));

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());

        return categoryRepositoryPort.save(existing);
    }

    @Override
    public void deleteById(UUID id) {
        if (categoryRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Categoria não encontrada com o ID: " + id);
        }
        categoryRepositoryPort.deleteById(id);
    }
}

