package com.iana.domain.ports.out;

import com.iana.domain.model.Book;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepositoryPort {

    public void save(Book book);

    public Optional<Book> findById(UUID id);

    public List<Book> findAll(Pageable pageable);

    List<Book> findAll();
}
