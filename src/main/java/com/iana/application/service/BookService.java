package com.iana.application.service;

import com.iana.domain.event.BookCreatedEvent;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Book;
import com.iana.domain.ports.in.CreateBookUseCase;
import com.iana.domain.ports.in.DeleteBookUseCase;
import com.iana.domain.ports.in.FindBookUseCase;
import com.iana.domain.ports.in.UpdateBookUseCase;
import com.iana.domain.ports.out.BookRepositoryPort;
import com.iana.domain.ports.out.EventPublisherPort;
import com.iana.domain.ports.out.LinkSafetyValidatorPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService implements CreateBookUseCase, FindBookUseCase, UpdateBookUseCase, DeleteBookUseCase {

    private final BookRepositoryPort bookRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private LinkSafetyValidatorPort linkSafetyValidatorPort;

    public BookService(BookRepositoryPort bookRepositoryPort, EventPublisherPort eventPublisherPort) {
        this.bookRepositoryPort = bookRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Autowired(required = false)
    public void setLinkSafetyValidatorPort(LinkSafetyValidatorPort linkSafetyValidatorPort) {
        this.linkSafetyValidatorPort = linkSafetyValidatorPort;
    }

    @Override
    public Book execute(Book book) {
        if (linkSafetyValidatorPort != null) {
            linkSafetyValidatorPort.validateSafety(book.getCoverUrl(), book.getTitle(), book.getDescription());
        }
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

    @Override
    public Book update(UUID id, Book book) {
        if (linkSafetyValidatorPort != null) {
            linkSafetyValidatorPort.validateSafety(book.getCoverUrl(), book.getTitle(), book.getDescription());
        }
        Book existing = bookRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com o ID: " + id));

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setDescription(book.getDescription());
        existing.setCoverUrl(book.getCoverUrl());
        existing.setCategoryId(book.getCategoryId());

        return bookRepositoryPort.save(existing);
    }

    @Override
    public void deleteById(UUID id) {
        if (bookRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Livro não encontrado com o ID: " + id);
        }
        bookRepositoryPort.deleteById(id);
    }
}



