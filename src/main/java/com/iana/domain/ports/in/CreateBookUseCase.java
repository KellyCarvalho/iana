package com.iana.domain.ports.in;

import com.iana.domain.model.Book;

public interface CreateBookUseCase {

    void execute (Book book);
}
