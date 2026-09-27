package com.iana.application.service;

import com.iana.domain.model.Book;
import com.iana.domain.ports.in.CreateBookUseCase;
import com.iana.domain.ports.out.BookRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class BookService implements CreateBookUseCase {

    private final BookRepositoryPort bookRepositoryPort;

    public BookService(BookRepositoryPort bookRepositoryPort) {
        this.bookRepositoryPort = bookRepositoryPort;
    }

    @Override
    public void execute(Book book) {
       bookRepositoryPort.save(book);
    }


}
