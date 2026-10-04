package com.iana.domain.ports.in;

import com.iana.domain.model.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindBookUseCase {

    Optional<Book> findById(UUID id);

    List<Book> findAll();
}

