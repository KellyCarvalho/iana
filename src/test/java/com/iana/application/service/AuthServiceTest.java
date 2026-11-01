package com.iana.application.service;

import com.iana.domain.exception.UnauthorizedException;
import com.iana.domain.model.User;
import com.iana.domain.ports.out.TokenServicePort;
import com.iana.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenServicePort tokenServicePort;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User(UUID.randomUUID(), "Kelly", "kelly@iana.com", "encoded-pass", "ROLE_USER", true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve autenticar usuário com credenciais válidas e retornar token JWT")
    void shouldAuthenticateSuccessfully() {
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("123456", "encoded-pass")).thenReturn(true);
        when(tokenServicePort.generateToken(activeUser)).thenReturn("jwt-token-mock");

        String token = authService.authenticate("kelly@iana.com", "123456");

        assertThat(token).isEqualTo("jwt-token-mock");
        verify(tokenServicePort, times(1)).generateToken(activeUser);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando e-mail não existir")
    void shouldThrowUnauthorizedWhenEmailNotFound() {
        when(userRepositoryPort.findByEmail("inexistente@iana.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("inexistente@iana.com", "123456"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("E-mail ou senha inválidos");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando e-mail ainda não foi verificado")
    void shouldThrowUnauthorizedWhenEmailNotVerified() {
        activeUser.setEmailVerified(false);
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.authenticate("kelly@iana.com", "123456"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("E-mail não verificado. Confirme seu e-mail antes de fazer login");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando usuário estiver inativo")
    void shouldThrowUnauthorizedWhenUserInactive() {
        activeUser.setActive(false);
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.authenticate("kelly@iana.com", "123456"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuário inativo");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando senha estiver incorreta")
    void shouldThrowUnauthorizedWhenPasswordInvalid() {
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("senha-errada", "encoded-pass")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate("kelly@iana.com", "senha-errada"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("E-mail ou senha inválidos");
    }
}

