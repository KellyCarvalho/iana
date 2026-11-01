package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.DeleteUserUseCase;
import com.iana.domain.ports.in.FindUserUseCase;
import com.iana.domain.ports.in.UpdateUserUseCase;
import com.iana.domain.ports.in.VerifyEmailUseCase;
import com.iana.domain.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService implements CreateUserUseCase, FindUserUseCase, UpdateUserUseCase, DeleteUserUseCase, VerifyEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User execute(User user) {
        validateEmailFormat(user.getEmail());

        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setActive(false);
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        User saved = userRepositoryPort.save(user);
        log.info("[Email Verification] Usuário cadastrado: email={}, verificationToken={}", saved.getEmail(), saved.getVerificationToken());
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
        return userRepositoryPort.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    @Override
    public User update(UUID id, User user) {
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        validateEmailFormat(user.getEmail());

        if (!existing.getEmail().equalsIgnoreCase(user.getEmail()) && userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail: " + user.getEmail());
        }

        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
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

    private void validateEmailFormat(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("Formato de e-mail inválido: " + email);
        }
    }
}



