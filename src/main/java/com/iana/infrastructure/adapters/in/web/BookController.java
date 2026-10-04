package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Book;
import com.iana.domain.ports.in.CreateBookUseCase;
import com.iana.domain.ports.in.FindBookUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateBookRequest;
import com.iana.infrastructure.adapters.in.web.dto.BookResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final CreateBookUseCase createBookUseCase;
    private final FindBookUseCase findBookUseCase;

    public BookController(CreateBookUseCase createBookUseCase, FindBookUseCase findBookUseCase) {
        this.createBookUseCase = createBookUseCase;
        this.findBookUseCase = findBookUseCase;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        Book book = new Book(null, request.getTitle(), request.getAuthor(), request.getDescription(), request.getCoverUrl(), request.getCategoryId(), null);

        Book createdBook = createBookUseCase.execute(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.fromDomain(createdBook));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable UUID id) {
        return findBookUseCase.findById(id)
                .map(BookResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com o ID: " + id));
    }


    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> responseList = findBookUseCase.findAll().stream()
                .map(BookResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }
}

