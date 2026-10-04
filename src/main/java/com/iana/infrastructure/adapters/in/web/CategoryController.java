package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Category;
import com.iana.domain.ports.in.CreateCategoryUseCase;
import com.iana.domain.ports.in.FindCategoryUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CategoryResponse;
import com.iana.infrastructure.adapters.in.web.dto.CreateCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final FindCategoryUseCase findCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase, FindCategoryUseCase findCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.findCategoryUseCase = findCategoryUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = new Category(null, request.getName(), request.getDescription());
        Category created = createCategoryUseCase.execute(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.fromDomain(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return findCategoryUseCase.findById(id)
                .map(CategoryResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + id));
    }


    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> list = findCategoryUseCase.findAll().stream()
                .map(CategoryResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
