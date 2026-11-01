package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.User;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User(userId, "Kelly", "kelly@iana.com", "123456", "ROLE_USER", true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve cadastrar usuário sempre com ROLE_USER, senha codificada e token de verificação")
    void shouldCreateUserWithEncodedPasswordAndVerificationToken() {
        user.setRole("ROLE_ADMIN");
        when(userRepositoryPort.existsByEmail("kelly@iana.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed-123456");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = userService.execute(user);

        assertThat(created.getPassword()).isEqualTo("hashed-123456");
        assertThat(created.getRole()).isEqualTo("ROLE_USER");
        assertThat(created.isActive()).isFalse();
        assertThat(created.isEmailVerified()).isFalse();
        assertThat(created.getVerificationToken()).isNotBlank();
        verify(userRepositoryPort, times(1)).save(user);
    }

    @Test
    @DisplayName("Deve alterar a role de um usuário existente via updateRole")
    void shouldUpdateUserRole() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateRole(userId, "ADMIN");

        assertThat(updated.getRole()).isEqualTo("ROLE_ADMIN");
        verify(userRepositoryPort, times(1)).save(user);
    }

    @Test
    @DisplayName("Deve confirmar e-mail com token válido e ativar o usuário")
    void shouldVerifyEmailAndActivateUser() {
        User pendingUser = new User(userId, "Kelly", "kelly@iana.com", "hashed", "ROLE_USER", false, false, "token-123", LocalDateTime.now());
        when(userRepositoryPort.findByVerificationToken("token-123")).thenReturn(Optional.of(pendingUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User verified = userService.verifyEmail("token-123");

        assertThat(verified.isEmailVerified()).isTrue();
        assertThat(verified.isActive()).isTrue();
        assertThat(verified.getVerificationToken()).isNull();
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao verificar e-mail com token inválido")
    void shouldThrowWhenVerifyingWithInvalidToken() {
        when(userRepositoryPort.findByVerificationToken("invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail("invalido"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token de verificação de e-mail inválido");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando formato de e-mail for inválido")
    void shouldThrowWhenEmailFormatIsInvalid() {
        user.setEmail("email-invalido-sem-dominio");

        assertThatThrownBy(() -> userService.execute(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Formato de e-mail inválido");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao cadastrar e-mail duplicado")
    void shouldThrowWhenCreatingDuplicateEmail() {
        when(userRepositoryPort.existsByEmail("kelly@iana.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.execute(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("kelly@iana.com");
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void shouldUpdateUser() {
        User updateData = new User(userId, "Kelly Carvalho", "kelly.novo@iana.com", "novaSenha", "ROLE_ADMIN", true, null);
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(userRepositoryPort.existsByEmail("kelly.novo@iana.com")).thenReturn(false);
        when(passwordEncoder.encode("novaSenha")).thenReturn("hashed-novaSenha");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.update(userId, updateData);

        assertThat(updated.getName()).isEqualTo("Kelly Carvalho");
        assertThat(updated.getEmail()).isEqualTo("kelly.novo@iana.com");
        assertThat(updated.getPassword()).isEqualTo("hashed-novaSenha");
        assertThat(updated.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao atualizar para um e-mail já em uso por outro usuário")
    void shouldThrowWhenUpdatingToExistingEmail() {
        User updateData = new User(userId, "Kelly", "outro@iana.com", null, "ROLE_USER", true, null);
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(userRepositoryPort.existsByEmail("outro@iana.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(userId, updateData))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Deve remover usuário por ID")
    void shouldDeleteUser() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepositoryPort).deleteById(userId);

        userService.deleteById(userId);

        verify(userRepositoryPort, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao remover usuário inexistente")
    void shouldThrowWhenDeletingNonExistentUser() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve buscar usuários por ID, e-mail e listar todos")
    void shouldFindUsers() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(user));
        when(userRepositoryPort.findAll()).thenReturn(List.of(user));

        assertThat(userService.findById(userId)).isPresent();
        assertThat(userService.findByEmail("kelly@iana.com")).isPresent();
        assertThat(userService.findAll()).hasSize(1);
    }
}
