package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.FindUserUseCase;
import com.iana.domain.ports.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements CreateUserUseCase, FindUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User execute(User user) {
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        return userRepositoryPort.save(user);
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
}
