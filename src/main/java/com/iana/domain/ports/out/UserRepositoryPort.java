package com.iana.domain.ports.out;

import com.iana.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String verificationToken);
    List<User> findAll();
    boolean existsByEmail(String email);
    void deleteById(UUID id);
}

