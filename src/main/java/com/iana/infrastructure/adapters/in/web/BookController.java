package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Book;
import com.iana.domain.model.Link;
import com.iana.domain.ports.in.CreateBookUseCase;
import com.iana.domain.ports.in.CreateLinkUseCase;
import com.iana.domain.ports.in.DeleteBookUseCase;
import com.iana.domain.ports.in.DeleteLinkUseCase;
import com.iana.domain.ports.in.FindBookUseCase;
import com.iana.domain.ports.in.FindLinkUseCase;
import com.iana.domain.ports.in.UpdateBookUseCase;
import com.iana.infrastructure.adapters.in.web.dto.BookResponse;
import com.iana.infrastructure.adapters.in.web.dto.CreateBookRequest;
import com.iana.infrastructure.adapters.in.web.dto.LinkResponse;
import com.iana.infrastructure.adapters.in.web.dto.UpdateBookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Endpoints para gerenciamento de livros")
public class BookController {

    private final CreateBookUseCase createBookUseCase;
    private final FindBookUseCase findBookUseCase;
    private final UpdateBookUseCase updateBookUseCase;
    private final DeleteBookUseCase deleteBookUseCase;
    private FindLinkUseCase findLinkUseCase;
    private CreateLinkUseCase createLinkUseCase;
    private DeleteLinkUseCase deleteLinkUseCase;

    public BookController(CreateBookUseCase createBookUseCase,
                          FindBookUseCase findBookUseCase,
                          UpdateBookUseCase updateBookUseCase,
                          DeleteBookUseCase deleteBookUseCase) {
        this.createBookUseCase = createBookUseCase;
        this.findBookUseCase = findBookUseCase;
        this.updateBookUseCase = updateBookUseCase;
        this.deleteBookUseCase = deleteBookUseCase;
    }

    @Autowired(required = false)
    public void setFindLinkUseCase(FindLinkUseCase findLinkUseCase) {
        this.findLinkUseCase = findLinkUseCase;
    }

    @Autowired(required = false)
    public void setCreateLinkUseCase(CreateLinkUseCase createLinkUseCase) {
        this.createLinkUseCase = createLinkUseCase;
    }

    @Autowired(required = false)
    public void setDeleteLinkUseCase(DeleteLinkUseCase deleteLinkUseCase) {
        this.deleteLinkUseCase = deleteLinkUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo livro")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        String effectiveCoverUrl = (request.getCoverUrl() != null && !request.getCoverUrl().isBlank())
                ? request.getCoverUrl()
                : request.getLinkUrl();
        Book book = new Book(null, request.getTitle(), request.getAuthor(), request.getDescription(), effectiveCoverUrl, request.getCategoryId(), null);

        Book createdBook = createBookUseCase.execute(book);
        syncBookLinks(createdBook, request.getLinkUrl(), request.getLinks(), false);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBookResponse(createdBook));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar livro por ID")
    public ResponseEntity<BookResponse> getBookById(@PathVariable UUID id) {
        return findBookUseCase.findById(id)
                .map(this::toBookResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado com o ID: " + id));
    }

    @GetMapping
    @Operation(summary = "Listar todos os livros")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> responseList = findBookUseCase.findAll().stream()
                .map(this::toBookResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um livro existente")
    public ResponseEntity<BookResponse> updateBook(@PathVariable UUID id, @Valid @RequestBody UpdateBookRequest request) {
        String effectiveCoverUrl = (request.getCoverUrl() != null && !request.getCoverUrl().isBlank())
                ? request.getCoverUrl()
                : request.getLinkUrl();
        Book book = new Book(id, request.getTitle(), request.getAuthor(), request.getDescription(), effectiveCoverUrl, request.getCategoryId(), null);
        Book updated = updateBookUseCase.update(id, book);
        syncBookLinks(updated, request.getLinkUrl(), request.getLinks(), true);
        return ResponseEntity.ok(toBookResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um livro por ID")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        deleteBookUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void syncBookLinks(Book book, String primaryLinkUrl, List<String> extraLinks, boolean replaceExisting) {
        if (book == null || book.getId() == null || createLinkUseCase == null) {
            return;
        }
        Set<String> urlsToSave = new LinkedHashSet<>();
        if (primaryLinkUrl != null && !primaryLinkUrl.isBlank()) {
            urlsToSave.add(primaryLinkUrl.trim());
        } else if (book.getCoverUrl() != null && !book.getCoverUrl().isBlank()) {
            urlsToSave.add(book.getCoverUrl().trim());
        }
        if (extraLinks != null) {
            for (String u : extraLinks) {
                if (u != null && !u.isBlank()) {
                    urlsToSave.add(u.trim());
                }
            }
        }
        if (replaceExisting && findLinkUseCase != null && deleteLinkUseCase != null && (!urlsToSave.isEmpty() || extraLinks != null)) {
            List<Link> existing = findLinkUseCase.findByBookId(book.getId());
            for (Link old : existing) {
                deleteLinkUseCase.deleteById(old.getId());
            }
        }
        for (String url : urlsToSave) {
            Link link = new Link(null, url, book.getTitle(), book.getDescription(), book.getId(), book.getCategoryId(), true, null);
            createLinkUseCase.execute(link);
        }
    }

    private BookResponse toBookResponse(Book book) {
        if (book == null) return null;
        List<LinkResponse> links = new ArrayList<>();
        if (findLinkUseCase != null && book.getId() != null) {
            links = findLinkUseCase.findByBookId(book.getId()).stream()
                    .map(LinkResponse::fromDomain)
                    .collect(Collectors.toList());
        }
        return BookResponse.fromDomain(book, links);
    }
}



