package com.iana.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.exception.UnauthorizedException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.AuthenticateUserUseCase;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.VerifyEmailUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateUserRequest;
import com.iana.infrastructure.adapters.in.web.dto.LoginRequest;
import com.iana.infrastructure.adapters.in.web.dto.VerifyEmailRequest;
import com.iana.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private VerifyEmailUseCase verifyEmailUseCase;

    @Test
    @DisplayName("POST /api/v1/auth/register deve criar usuário pendente de verificação de e-mail com ROLE_USER")
    void shouldRegisterUserAndReturnVerificationToken() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Kelly", "kelly@iana.com", "Senha@123");
        User created = new User(UUID.randomUUID(), "Kelly", "kelly@iana.com", "hashed", "ROLE_USER", false, false, "verify-token-abc", LocalDateTime.now());
        when(createUserUseCase.execute(any(User.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("kelly@iana.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.verificationToken").value("verify-token-abc"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register deve retornar 400 quando senha não tiver maiúscula, número e caractere especial")
    void shouldRejectWeakPasswordOnRegister() throws Exception {
        CreateUserRequest weakRequest = new CreateUserRequest("Kelly", "kelly@iana.com", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/verify-email deve confirmar o e-mail e ativar o usuário")
    void shouldVerifyEmailViaPost() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("verify-token-abc");
        User verified = new User(UUID.randomUUID(), "Kelly", "kelly@iana.com", "hashed", "ROLE_USER", true, true, null, LocalDateTime.now());
        when(verifyEmailUseCase.verifyEmail("verify-token-abc")).thenReturn(verified);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/auth/verify-email?token=... deve confirmar o e-mail via query param")
    void shouldVerifyEmailViaGetQuery() throws Exception {
        User verified = new User(UUID.randomUUID(), "Kelly", "kelly@iana.com", "hashed", "ROLE_USER", true, true, null, LocalDateTime.now());
        when(verifyEmailUseCase.verifyEmail("verify-token-abc")).thenReturn(verified);

        mockMvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", "verify-token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login deve retornar 200 OK com token JWT")
    void shouldLoginAndReturnJwt() throws Exception {
        LoginRequest request = new LoginRequest("kelly@iana.com", "123456");
        when(authenticateUserUseCase.authenticate("kelly@iana.com", "123456")).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login deve retornar 401 quando credenciais forem inválidas")
    void shouldReturn401WhenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("kelly@iana.com", "wrong");
        when(authenticateUserUseCase.authenticate("kelly@iana.com", "wrong"))
                .thenThrow(new UnauthorizedException("E-mail ou senha inválidos"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }
}

