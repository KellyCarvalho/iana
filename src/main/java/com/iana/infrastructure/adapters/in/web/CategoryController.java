package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Category;
import com.iana.domain.ports.in.CreateCategoryUseCase;
import com.iana.domain.ports.in.DeleteCategoryUseCase;
import com.iana.domain.ports.in.FindCategoryUseCase;
import com.iana.domain.ports.in.FindLinkUseCase;
import com.iana.domain.ports.in.UpdateCategoryUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CategoryResponse;
import com.iana.infrastructure.adapters.in.web.dto.CreateCategoryRequest;
import com.iana.infrastructure.adapters.in.web.dto.LinkResponse;
import com.iana.infrastructure.adapters.in.web.dto.UpdateCategoryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Endpoints para gerenciamento de categorias")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final FindCategoryUseCase findCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private FindLinkUseCase findLinkUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase,
                              FindCategoryUseCase findCategoryUseCase,
                              UpdateCategoryUseCase updateCategoryUseCase,
                              DeleteCategoryUseCase deleteCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.findCategoryUseCase = findCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

    @Autowired(required = false)
    public void setFindLinkUseCase(FindLinkUseCase findLinkUseCase) {
        this.findLinkUseCase = findLinkUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma nova categoria")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = new Category(null, request.getName(), request.getDescription());
        Category created = createCategoryUseCase.execute(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCategoryResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return findCategoryUseCase.findById(id)
                .map(this::toCategoryResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + id));
    }

    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> list = findCategoryUseCase.findAll().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma categoria existente")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        Category category = new Category(id, request.getName(), request.getDescription());
        Category updated = updateCategoryUseCase.update(id, category);
        return ResponseEntity.ok(toCategoryResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover uma categoria por ID")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        deleteCategoryUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        if (category == null) return null;
        List<LinkResponse> links = Collections.emptyList();
        if (findLinkUseCase != null && category.getId() != null) {
            links = findLinkUseCase.findByCategoryId(category.getId()).stream()
                    .map(LinkResponse::fromDomain)
                    .collect(Collectors.toList());
        }
        return CategoryResponse.fromDomain(category, links);
    }
}


