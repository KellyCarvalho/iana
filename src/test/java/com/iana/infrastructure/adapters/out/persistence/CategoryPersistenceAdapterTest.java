package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Category;
import com.iana.infrastructure.adapters.out.persistence.entity.CategoryJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.CategoryPersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataCategoryRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryPersistenceAdapterTest {

    @Mock
    private SpringDataCategoryRepository repository;

    @Mock
    private CategoryPersistenceMapper mapper;

    @InjectMocks
    private CategoryPersistenceAdapter adapter;

    private UUID categoryId;
    private Category category;
    private CategoryJpaEntity categoryJpaEntity;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category(categoryId, "Back-end", "Conteúdo sobre APIs e Servidores");
        categoryJpaEntity = new CategoryJpaEntity(categoryId, "Back-end", "Conteúdo sobre APIs e Servidores");
    }

    @Test
    @DisplayName("Deve salvar categoria através do adapter")
    void shouldSaveCategory() {
        when(mapper.toEntity(category)).thenReturn(categoryJpaEntity);
        when(repository.save(categoryJpaEntity)).thenReturn(categoryJpaEntity);
        when(mapper.toDomain(categoryJpaEntity)).thenReturn(category);

        Category result = adapter.save(category);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Back-end");
        verify(repository, times(1)).save(categoryJpaEntity);
    }

    @Test
    @DisplayName("Deve buscar categoria por ID via adapter")
    void shouldFindById() {
        when(repository.findById(categoryId)).thenReturn(Optional.of(categoryJpaEntity));
        when(mapper.toDomain(categoryJpaEntity)).thenReturn(category);

        Optional<Category> result = adapter.findById(categoryId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("Deve deletar categoria por ID")
    void shouldDeleteById() {
        doNothing().when(repository).deleteById(categoryId);

        adapter.deleteById(categoryId);

        verify(repository, times(1)).deleteById(categoryId);
    }
}
