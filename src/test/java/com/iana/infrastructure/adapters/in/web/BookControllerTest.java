package com.iana.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.model.Book;
import com.iana.domain.ports.in.CreateBookUseCase;
import com.iana.domain.ports.in.DeleteBookUseCase;
import com.iana.domain.ports.in.FindBookUseCase;
import com.iana.domain.ports.in.UpdateBookUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateBookRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateBookRequest;
import com.iana.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateBookUseCase createBookUseCase;

    @MockitoBean
    private FindBookUseCase findBookUseCase;

    @MockitoBean
    private UpdateBookUseCase updateBookUseCase;

    @MockitoBean
    private DeleteBookUseCase deleteBookUseCase;

    private UUID bookId;
    private Book book;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        book = new Book(bookId, "Clean Code", "Robert C. Martin", "Descrição", "http://cover.jpg", null, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve retornar 401 quando acessar /api/v1/books sem token JWT")
    void shouldReturn401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/books deve criar livro quando autenticado")
    void shouldCreateBook() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert C. Martin", "Descrição", "http://cover.jpg", null);
        when(createBookUseCase.execute(any(Book.class))).thenReturn(book);

        mockMvc.perform(post("/api/v1/books")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} deve retornar livro por ID")
    void shouldGetBookById() throws Exception {
        when(findBookUseCase.findById(bookId)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/v1/books/{id}", bookId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/v1/books deve listar livros")
    void shouldGetAllBooks() throws Exception {
        when(findBookUseCase.findAll()).thenReturn(List.of(book));

        mockMvc.perform(get("/api/v1/books")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} deve atualizar livro")
    void shouldUpdateBook() throws Exception {
        UpdateBookRequest request = new UpdateBookRequest("Clean Architecture", "Robert C. Martin", "Nova desc", null, null);
        Book updated = new Book(bookId, "Clean Architecture", "Robert C. Martin", "Nova desc", null, null, LocalDateTime.now());
        when(updateBookUseCase.update(eq(bookId), any(Book.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Architecture"));
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} deve remover livro e retornar 204")
    void shouldDeleteBook() throws Exception {
        doNothing().when(deleteBookUseCase).deleteById(bookId);

        mockMvc.perform(delete("/api/v1/books/{id}", bookId)
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }
}
