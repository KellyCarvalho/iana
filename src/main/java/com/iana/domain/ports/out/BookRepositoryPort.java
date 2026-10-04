package com.iana.domain.ports.out;

import com.iana.domain.model.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepositoryPort {

    Book save(Book book);

    Optional<Book> findById(UUID id);

    List<Book> findAll();

    void deleteById(UUID id);
}

