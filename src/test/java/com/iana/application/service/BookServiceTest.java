package com.iana.application.service;

import com.iana.domain.event.BookCreatedEvent;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Book;
import com.iana.domain.ports.out.BookRepositoryPort;
import com.iana.domain.ports.out.EventPublisherPort;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepositoryPort bookRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private BookService bookService;

    private UUID bookId;
    private Book book;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        book = new Book(bookId, "Clean Code", "Robert C. Martin", "Descrição", "http://cover.jpg", UUID.randomUUID(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar um livro e publicar evento BookCreatedEvent")
    void shouldCreateBookAndPublishEvent() {
        when(bookRepositoryPort.save(book)).thenReturn(book);

        Book saved = bookService.execute(book);

        assertThat(saved).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Clean Code");
        verify(bookRepositoryPort, times(1)).save(book);
        verify(eventPublisherPort, times(1)).publishBookCreated(any(BookCreatedEvent.class));
    }

    @Test
    @DisplayName("Deve buscar livro por ID")
    void shouldFindBookById() {
        when(bookRepositoryPort.findById(bookId)).thenReturn(Optional.of(book));

        Optional<Book> found = bookService.findById(bookId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(bookId);
    }

    @Test
    @DisplayName("Deve listar todos os livros")
    void shouldFindAllBooks() {
        when(bookRepositoryPort.findAll()).thenReturn(List.of(book));

        List<Book> books = bookService.findAll();

        assertThat(books).hasSize(1);
    }

    @Test
    @DisplayName("Deve atualizar um livro existente")
    void shouldUpdateBook() {
        Book updatedData = new Book(bookId, "Clean Architecture", "Uncle Bob", "Nova desc", "http://new.jpg", null, null);
        when(bookRepositoryPort.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepositoryPort.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book updated = bookService.update(bookId, updatedData);

        assertThat(updated.getTitle()).isEqualTo("Clean Architecture");
        assertThat(updated.getAuthor()).isEqualTo("Uncle Bob");
        verify(bookRepositoryPort, times(1)).save(book);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar livro inexistente")
    void shouldThrowWhenUpdatingNonExistentBook() {
        when(bookRepositoryPort.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(bookId, book))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve deletar um livro existente")
    void shouldDeleteBook() {
        when(bookRepositoryPort.findById(bookId)).thenReturn(Optional.of(book));
        doNothing().when(bookRepositoryPort).deleteById(bookId);

        bookService.deleteById(bookId);

        verify(bookRepositoryPort, times(1)).deleteById(bookId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao deletar livro inexistente")
    void shouldThrowWhenDeletingNonExistentBook() {
        when(bookRepositoryPort.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteById(bookId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
