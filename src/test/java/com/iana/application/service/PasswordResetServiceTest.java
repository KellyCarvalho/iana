package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.User;
import com.iana.domain.ports.out.EmailSenderPort;
import com.iana.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailSenderPort emailSenderPort;

    private PasswordResetService passwordResetService;
    private User existingUser;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(userRepositoryPort, passwordEncoder);
        passwordResetService.setEmailSenderPort(emailSenderPort);

        existingUser = new User(
                UUID.randomUUID(),
                "Kelly",
                "kelly@iana.com",
                "old-hash",
                "ROLE_USER",
                true,
                true,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve gerar código de 6 dígitos e enviar por e-mail ao solicitar redefinição de senha")
    void shouldGenerateAndSendCodeOnRequestPasswordReset() {
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.requestPasswordReset("kelly@iana.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        String generatedCode = userCaptor.getValue().getVerificationToken();
        assertNotNull(generatedCode);
        assertTrue(generatedCode.matches("^\\d{6}$"), "Código deve conter exatamente 6 dígitos");

        verify(emailSenderPort).sendVerificationCode(eq("kelly@iana.com"), eq("Kelly"), eq(generatedCode));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando e-mail não existe ao solicitar código")
    void shouldThrowWhenEmailNotFoundOnRequestPasswordReset() {
        when(userRepositoryPort.findByEmail("inexistente@iana.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> passwordResetService.requestPasswordReset("inexistente@iana.com"));
        verify(emailSenderPort, never()).sendVerificationCode(any(), any(), any());
    }

    @Test
    @DisplayName("Deve validar código de verificação corretamente")
    void shouldVerifyResetCodeSuccessfully() {
        existingUser.setVerificationToken("123456");
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(existingUser));

        boolean valid = passwordResetService.verifyResetCode("kelly@iana.com", "123456");
        assertTrue(valid);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando código de verificação está incorreto")
    void shouldThrowWhenResetCodeIsInvalid() {
        existingUser.setVerificationToken("123456");
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(existingUser));

        assertThrows(BusinessException.class,
                () -> passwordResetService.verifyResetCode("kelly@iana.com", "654321"));
    }

    @Test
    @DisplayName("Deve redefinir senha com sucesso quando código e regras de senha são válidos")
    void shouldResetPasswordSuccessfully() {
        existingUser.setVerificationToken("123456");
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("NovaSenha#123")).thenReturn("new-encoded-hash");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.resetPassword("kelly@iana.com", "123456", "NovaSenha#123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertEquals("new-encoded-hash", saved.getPassword());
        assertNull(saved.getVerificationToken(), "Token deve ser limpo após redefinir a senha");
        assertTrue(saved.isEmailVerified());
        assertTrue(saved.isActive());
    }

    @Test
    @DisplayName("Deve rejeitar nova senha que não cumpre as regras de complexidade")
    void shouldRejectWeakPasswordOnReset() {
        existingUser.setVerificationToken("123456");
        when(userRepositoryPort.findByEmail("kelly@iana.com")).thenReturn(Optional.of(existingUser));

        assertThrows(BusinessException.class,
                () -> passwordResetService.resetPassword("kelly@iana.com", "123456", "fraca123"));
        verify(userRepositoryPort, never()).save(any());
    }
}
