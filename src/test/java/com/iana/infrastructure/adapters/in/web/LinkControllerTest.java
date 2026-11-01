package com.iana.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.model.Link;
import com.iana.domain.ports.in.CreateLinkUseCase;
import com.iana.domain.ports.in.DeleteLinkUseCase;
import com.iana.domain.ports.in.FindLinkUseCase;
import com.iana.domain.ports.in.UpdateLinkUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateLinkRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateLinkRequest;
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

@WebMvcTest(LinkController.class)
@Import(SecurityConfig.class)
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateLinkUseCase createLinkUseCase;

    @MockitoBean
    private FindLinkUseCase findLinkUseCase;

    @MockitoBean
    private UpdateLinkUseCase updateLinkUseCase;

    @MockitoBean
    private DeleteLinkUseCase deleteLinkUseCase;

    private UUID linkId;
    private UUID bookId;
    private UUID categoryId;
    private Link link;

    @BeforeEach
    void setUp() {
        linkId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        link = new Link(linkId, "https://spring.io", "Spring", "Docs", bookId, categoryId, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/v1/links deve criar link")
    void shouldCreateLink() throws Exception {
        CreateLinkRequest request = new CreateLinkRequest("https://spring.io", "Spring", "Docs", bookId, categoryId);
        when(createLinkUseCase.execute(any(Link.class))).thenReturn(link);

        mockMvc.perform(post("/api/v1/links")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://spring.io"));
    }

    @Test
    @DisplayName("GET /api/v1/links/{id} deve buscar link por ID")
    void shouldGetLinkById() throws Exception {
        when(findLinkUseCase.findById(linkId)).thenReturn(Optional.of(link));

        mockMvc.perform(get("/api/v1/links/{id}", linkId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://spring.io"));
    }

    @Test
    @DisplayName("GET /api/v1/links deve listar links filtrando por bookId")
    void shouldGetLinksByBookId() throws Exception {
        when(findLinkUseCase.findByBookId(bookId)).thenReturn(List.of(link));

        mockMvc.perform(get("/api/v1/links")
                        .param("bookId", bookId.toString())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(bookId.toString()));
    }

    @Test
    @DisplayName("PUT /api/v1/links/{id} deve atualizar link")
    void shouldUpdateLink() throws Exception {
        UpdateLinkRequest request = new UpdateLinkRequest("https://docs.spring.io", "Spring Docs", "Updated", bookId, categoryId);
        Link updated = new Link(linkId, "https://docs.spring.io", "Spring Docs", "Updated", bookId, categoryId, true, LocalDateTime.now());
        when(updateLinkUseCase.update(eq(linkId), any(Link.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/links/{id}", linkId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://docs.spring.io"));
    }

    @Test
    @DisplayName("DELETE /api/v1/links/{id} deve remover link e retornar 204")
    void shouldDeleteLink() throws Exception {
        doNothing().when(deleteLinkUseCase).deleteById(linkId);

        mockMvc.perform(delete("/api/v1/links/{id}", linkId)
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }
}
