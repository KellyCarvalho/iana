package com.iana.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.model.Category;
import com.iana.domain.ports.in.CreateCategoryUseCase;
import com.iana.domain.ports.in.DeleteCategoryUseCase;
import com.iana.domain.ports.in.FindCategoryUseCase;
import com.iana.domain.ports.in.UpdateCategoryUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateCategoryRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateCategoryRequest;
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

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCategoryUseCase createCategoryUseCase;

    @MockitoBean
    private FindCategoryUseCase findCategoryUseCase;

    @MockitoBean
    private UpdateCategoryUseCase updateCategoryUseCase;

    @MockitoBean
    private DeleteCategoryUseCase deleteCategoryUseCase;

    private UUID categoryId;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category(categoryId, "Back-end", "APIs e Microsserviços");
    }

    @Test
    @DisplayName("POST /api/v1/categories deve criar categoria")
    void shouldCreateCategory() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Back-end", "APIs e Microsserviços");
        when(createCategoryUseCase.execute(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/api/v1/categories")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Back-end"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} deve retornar categoria por ID")
    void shouldGetCategoryById() throws Exception {
        when(findCategoryUseCase.findById(categoryId)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Back-end"));
    }

    @Test
    @DisplayName("GET /api/v1/categories deve listar categorias")
    void shouldGetAllCategories() throws Exception {
        when(findCategoryUseCase.findAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/categories")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Back-end"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} deve atualizar categoria")
    void shouldUpdateCategory() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Cloud", "AWS e Containers");
        Category updated = new Category(categoryId, "Cloud", "AWS e Containers");
        when(updateCategoryUseCase.update(eq(categoryId), any(Category.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cloud"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} deve remover categoria e retornar 204")
    void shouldDeleteCategory() throws Exception {
        doNothing().when(deleteCategoryUseCase).deleteById(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }
}
