package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.CategoryJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SpringDataCategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataCategoryRepository repository;

    @Test
    @DisplayName("Deve salvar uma categoria com sucesso")
    void shouldSaveCategory() {
        CategoryJpaEntity category = new CategoryJpaEntity(null, "Tecnologia", "Livros e links de tecnologia");

        CategoryJpaEntity saved = repository.save(category);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Tecnologia");
        assertThat(saved.getDescription()).isEqualTo("Livros e links de tecnologia");
    }

    @Test
    @DisplayName("Deve buscar categoria por ID")
    void shouldFindCategoryById() {
        CategoryJpaEntity category = new CategoryJpaEntity(null, "Ficção", "Livros de ficção cientifica");
        CategoryJpaEntity persisted = entityManager.persistAndFlush(category);

        Optional<CategoryJpaEntity> found = repository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Ficção");
    }

    @Test
    @DisplayName("Deve listar todas as categorias")
    void shouldFindAllCategories() {
        entityManager.persist(new CategoryJpaEntity(null, "Cat 1", "Desc 1"));
        entityManager.persist(new CategoryJpaEntity(null, "Cat 2", "Desc 2"));
        entityManager.flush();

        List<CategoryJpaEntity> list = repository.findAll();

        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("Deve falhar ao tentar salvar categoria com nome duplicado")
    void shouldFailOnDuplicateCategoryName() {
        CategoryJpaEntity cat1 = new CategoryJpaEntity(null, "Design", "Design gráfico");
        entityManager.persistAndFlush(cat1);

        CategoryJpaEntity cat2 = new CategoryJpaEntity(null, "Design", "Outro design");

        assertThatThrownBy(() -> repository.saveAndFlush(cat2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
