package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.PasswordResetUseCase;
import com.iana.domain.ports.out.EmailSenderPort;
import com.iana.domain.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@Service
public class PasswordResetService implements PasswordResetUseCase {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private EmailSenderPort emailSenderPort;

    public PasswordResetService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired(required = false)
    public void setEmailSenderPort(EmailSenderPort emailSenderPort) {
        this.emailSenderPort = emailSenderPort;
    }

    @Override
    public void requestPasswordReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        User user = userRepositoryPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuário encontrado com o e-mail informado."));

        String verificationCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        user.setVerificationToken(verificationCode);
        userRepositoryPort.save(user);

        if (emailSenderPort != null) {
            emailSenderPort.sendVerificationCode(user.getEmail(), user.getName(), verificationCode);
        }
        log.info("[Password Reset] Código de recuperação gerado para {}: {}", user.getEmail(), verificationCode);
    }

    @Override
    public boolean verifyResetCode(String email, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("O código de verificação é obrigatório.");
        }
        String normalizedEmail = normalizeEmail(email);
        User user = userRepositoryPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuário encontrado com o e-mail informado."));

        String trimmedCode = code.trim();
        if (user.getVerificationToken() == null || !user.getVerificationToken().equals(trimmedCode)) {
            throw new BusinessException("Código de verificação inválido ou já utilizado.");
        }
        return true;
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        verifyResetCode(email, code);

        if (newPassword == null || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new BusinessException("A senha deve conter no mínimo 8 caracteres, incluindo pelo menos uma letra maiúscula, um número e um caractere especial.");
        }

        String normalizedEmail = normalizeEmail(email);
        User user = userRepositoryPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuário encontrado com o e-mail informado."));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationToken(null);
        user.setEmailVerified(true);
        user.setActive(true);
        userRepositoryPort.save(user);

        log.info("[Password Reset] Senha redefinida com sucesso para o usuário: {}", user.getEmail());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("O e-mail é obrigatório.");
        }
        return email.trim().toLowerCase();
    }
}
