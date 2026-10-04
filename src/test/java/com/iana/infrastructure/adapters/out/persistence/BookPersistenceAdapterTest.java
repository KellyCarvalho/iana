package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Book;
import com.iana.infrastructure.adapters.out.persistence.entity.BookJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.BookPersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookPersistenceAdapterTest {

    @Mock
    private SpringDataBookRepository repository;

    @Mock
    private BookPersistenceMapper mapper;

    @InjectMocks
    private BookPersistenceAdapter adapter;

    private UUID bookId;
    private Book book;
    private BookJpaEntity bookJpaEntity;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        book = new Book(bookId, "Clean Architecture", "Robert C. Martin", "Structure and design", "http://cover.jpg", null, LocalDateTime.now());
        bookJpaEntity = new BookJpaEntity(bookId, "Clean Architecture", "Robert C. Martin", "Structure and design", "http://cover.jpg", null, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve salvar um livro via adapter")
    void shouldSaveBook() {
        when(mapper.toEntity(book)).thenReturn(bookJpaEntity);
        when(repository.save(bookJpaEntity)).thenReturn(bookJpaEntity);
        when(mapper.toDomain(bookJpaEntity)).thenReturn(book);

        Book saved = adapter.save(book);

        assertThat(saved).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Clean Architecture");
        verify(repository, times(1)).save(bookJpaEntity);
    }

    @Test
    @DisplayName("Deve buscar livro por ID via adapter")
    void shouldFindById() {
        when(repository.findById(bookId)).thenReturn(Optional.of(bookJpaEntity));
        when(mapper.toDomain(bookJpaEntity)).thenReturn(book);

        Optional<Book> found = adapter.findById(bookId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(bookId);
    }
}
