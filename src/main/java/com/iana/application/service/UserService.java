package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.DeleteUserUseCase;
import com.iana.domain.ports.in.FindUserUseCase;
import com.iana.domain.ports.in.UpdateUserUseCase;
import com.iana.domain.ports.in.VerifyEmailUseCase;
import com.iana.domain.ports.out.EmailSenderPort;
import com.iana.domain.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService implements CreateUserUseCase, FindUserUseCase, UpdateUserUseCase, DeleteUserUseCase, VerifyEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private EmailSenderPort emailSenderPort;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired(required = false)
    public void setEmailSenderPort(EmailSenderPort emailSenderPort) {
        this.emailSenderPort = emailSenderPort;
    }

    @Override
    public User execute(User user) {
        String normalizedEmail = normalizeEmail(user.getEmail());
        user.setEmail(normalizedEmail);
        validateEmailFormat(normalizedEmail);

        if (userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail: " + normalizedEmail);
        }

        String verificationCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setActive(false);
        user.setEmailVerified(false);
        user.setVerificationToken(verificationCode);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        User saved = userRepositoryPort.save(user);
        if (emailSenderPort != null) {
            emailSenderPort.sendVerificationCode(saved.getEmail(), saved.getName(), verificationCode);
        }
        log.info("[Email Verification] Usuário cadastrado: email={}, codigoVerificacao={}", saved.getEmail(), saved.getVerificationToken());
        return saved;
    }

    @Override
    public User verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("O token de verificação é obrigatório");
        }

        User user = userRepositoryPort.findByVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Token de verificação de e-mail inválido ou já utilizado"));

        user.setEmailVerified(true);
        user.setActive(true);
        user.setVerificationToken(null);

        User verifiedUser = userRepositoryPort.save(user);
        log.info("[Email Verification] E-mail confirmado e conta ativada com sucesso: email={}", verifiedUser.getEmail());
        return verifiedUser;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepositoryPort.findByEmail(normalizeEmail(email));
    }

    @Override
    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    @Override
    public User update(UUID id, User user) {
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        String normalizedEmail = normalizeEmail(user.getEmail());
        validateEmailFormat(normalizedEmail);

        if (!existing.getEmail().equalsIgnoreCase(normalizedEmail) && userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail: " + normalizedEmail);
        }

        existing.setName(user.getName());
        existing.setEmail(normalizedEmail);
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() != null && !user.getRole().isBlank()) {
            existing.setRole(normalizeRole(user.getRole()));
        }
        existing.setActive(user.isActive());

        return userRepositoryPort.save(existing);
    }

    @Override
    public User updateRole(UUID id, String role) {
        if (role == null || role.isBlank()) {
            throw new BusinessException("A role é obrigatória");
        }
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        existing.setRole(normalizeRole(role));
        return userRepositoryPort.save(existing);
    }

    @Override
    public void deleteById(UUID id) {
        if (userRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        userRepositoryPort.deleteById(id);
    }

    private String normalizeRole(String role) {
        String upper = role.trim().toUpperCase();
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }

    private void validateEmailFormat(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("Formato de e-mail inválido: " + email);
        }
    }
}



