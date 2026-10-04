package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.BookJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpringDataBookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataBookRepository repository;

    @Test
    @DisplayName("Deve salvar um livro com sucesso")
    void shouldSaveBook() {
        BookJpaEntity book = new BookJpaEntity(null, "Clean Code", "Robert C. Martin", "A Handbook of Agile Software Craftsmanship", "http://cover.url", UUID.randomUUID(), LocalDateTime.now());

        BookJpaEntity saved = repository.save(book);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Clean Code");
        assertThat(saved.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar livro por ID")
    void shouldFindBookById() {
        BookJpaEntity book = new BookJpaEntity(null, "DDD", "Eric Evans", "Domain-Driven Design", null, null, LocalDateTime.now());
        BookJpaEntity persisted = entityManager.persistAndFlush(book);

        Optional<BookJpaEntity> found = repository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("DDD");
    }

    @Test
    @DisplayName("Deve retornar todos os livros")
    void shouldFindAllBooks() {
        entityManager.persist(new BookJpaEntity(null, "Livro 1", "Autor 1", "Desc 1", null, null, LocalDateTime.now()));
        entityManager.persist(new BookJpaEntity(null, "Livro 2", "Autor 2", "Desc 2", null, null, LocalDateTime.now()));
        entityManager.flush();

        List<BookJpaEntity> books = repository.findAll();

        assertThat(books).hasSize(2);
    }
}
