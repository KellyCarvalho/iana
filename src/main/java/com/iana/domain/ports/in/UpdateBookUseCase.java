package com.iana.domain.ports.in;

import com.iana.domain.model.Book;

import java.util.UUID;

public interface UpdateBookUseCase {
    Book update(UUID id, Book book);
}
