package com.iana.application.service;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Category;
import com.iana.domain.ports.out.CategoryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private CategoryService categoryService;

    private UUID categoryId;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category(categoryId, "Arquitetura", "Livros de Arquitetura de Software");
    }

    @Test
    @DisplayName("Deve criar uma categoria")
    void shouldCreateCategory() {
        when(categoryRepositoryPort.save(category)).thenReturn(category);

        Category saved = categoryService.execute(category);

        assertThat(saved.getName()).isEqualTo("Arquitetura");
        verify(categoryRepositoryPort, times(1)).save(category);
    }

    @Test
    @DisplayName("Deve buscar categoria por ID")
    void shouldFindCategoryById() {
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));

        Optional<Category> found = categoryService.findById(categoryId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("Deve listar todas as categorias")
    void shouldFindAllCategories() {
        when(categoryRepositoryPort.findAll()).thenReturn(List.of(category));

        List<Category> list = categoryService.findAll();

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Deve atualizar uma categoria existente")
    void shouldUpdateCategory() {
        Category updateData = new Category(categoryId, "DevOps", "Containers e CI/CD");
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepositoryPort.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category updated = categoryService.update(categoryId, updateData);

        assertThat(updated.getName()).isEqualTo("DevOps");
        assertThat(updated.getDescription()).isEqualTo("Containers e CI/CD");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar categoria inexistente")
    void shouldThrowWhenUpdatingNonExistentCategory() {
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(categoryId, category))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve remover uma categoria existente")
    void shouldDeleteCategory() {
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepositoryPort).deleteById(categoryId);

        categoryService.deleteById(categoryId);

        verify(categoryRepositoryPort, times(1)).deleteById(categoryId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao remover categoria inexistente")
    void shouldThrowWhenDeletingNonExistentCategory() {
        when(categoryRepositoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
