package com.iana.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.DeleteUserUseCase;
import com.iana.domain.ports.in.FindUserUseCase;
import com.iana.domain.ports.in.UpdateUserUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateUserRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateUserRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateUserRoleRequest;
import com.iana.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private FindUserUseCase findUserUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User(userId, "Kelly", "kelly@iana.com", "hashed", "ROLE_USER", true, LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/v1/users deve ser público (sem JWT) e atribuir ROLE_USER por padrão")
    void shouldCreateUserWithoutJwt() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Kelly", "kelly@iana.com", "Senha@123");
        when(createUserUseCase.execute(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("kelly@iana.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("GET /api/v1/users deve exigir autenticação JWT (401 sem token)")
    void shouldReturn401OnGetUsersWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} deve buscar usuário quando autenticado")
    void shouldGetUserById() throws Exception {
        when(findUserUseCase.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("kelly@iana.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users deve listar todos os usuários quando autenticado")
    void shouldGetAllUsers() throws Exception {
        when(findUserUseCase.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/users")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("kelly@iana.com"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} deve retornar 403 quando usuário comum tentar alterar a role")
    void shouldReturn403WhenNonAdminTriesToUpdateRole() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Kelly Carvalho", "kelly@iana.com", null, "ROLE_ADMIN", true);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} deve permitir alterar a role quando autenticado como ROLE_ADMIN")
    void shouldAllowAdminToUpdateRoleInPut() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Kelly Carvalho", "kelly@iana.com", null, "ROLE_ADMIN", true);
        User updated = new User(userId, "Kelly Carvalho", "kelly@iana.com", "hashed", "ROLE_ADMIN", true, LocalDateTime.now());
        when(updateUserUseCase.update(eq(userId), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kelly Carvalho"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/role deve alterar a role quando autenticado como ROLE_ADMIN")
    void shouldUpdateRoleWhenAdmin() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest("ROLE_ADMIN");
        User updated = new User(userId, "Kelly", "kelly@iana.com", "hashed", "ROLE_ADMIN", true, LocalDateTime.now());
        when(updateUserUseCase.updateRole(userId, "ROLE_ADMIN")).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/{id}/role", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/role deve retornar 403 quando usuário não for ROLE_ADMIN")
    void shouldReturn403OnPatchRoleWhenNotAdmin() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest("ROLE_ADMIN");

        mockMvc.perform(patch("/api/v1/users/{id}/role", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} deve remover usuário e retornar 204 quando for ROLE_ADMIN")
    void shouldDeleteUser() throws Exception {
        doNothing().when(deleteUserUseCase).deleteById(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} deve retornar 403 quando usuário não for ROLE_ADMIN")
    void shouldReturn403OnDeleteUserWhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }
}

