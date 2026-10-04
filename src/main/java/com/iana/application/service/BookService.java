package com.iana.application.service;

import com.iana.domain.event.BookCreatedEvent;
import com.iana.domain.model.Book;
import com.iana.domain.ports.in.CreateBookUseCase;
import com.iana.domain.ports.in.FindBookUseCase;
import com.iana.domain.ports.out.BookRepositoryPort;
import com.iana.domain.ports.out.EventPublisherPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService implements CreateBookUseCase, FindBookUseCase {

    private final BookRepositoryPort bookRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    public BookService(BookRepositoryPort bookRepositoryPort, EventPublisherPort eventPublisherPort) {
        this.bookRepositoryPort = bookRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public Book execute(Book book) {
        if (book.getCreatedAt() == null) {
            book.setCreatedAt(LocalDateTime.now());
        }
        Book savedBook = bookRepositoryPort.save(book);

        // Disparo de evento de domínio
        if (eventPublisherPort != null) {
            eventPublisherPort.publishBookCreated(new BookCreatedEvent(
                    savedBook.getId(),
                    savedBook.getTitle(),
                    savedBook.getAuthor(),
                    savedBook.getCategoryId()
            ));
        }

        return savedBook;
    }


    @Override
    public Optional<Book> findById(UUID id) {
        return bookRepositoryPort.findById(id);
    }

    @Override
    public List<Book> findAll() {
        return bookRepositoryPort.findAll();
    }
}


