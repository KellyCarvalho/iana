package com.iana.application.service;

import com.iana.domain.exception.UnauthorizedException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.AuthenticateUserUseCase;
import com.iana.domain.ports.out.TokenServicePort;
import com.iana.domain.ports.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenServicePort tokenServicePort;

    public AuthService(UserRepositoryPort userRepositoryPort,
                       PasswordEncoder passwordEncoder,
                       TokenServicePort tokenServicePort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.tokenServicePort = tokenServicePort;
    }

    @Override
    public String authenticate(String email, String password) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos"));

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("E-mail não verificado. Confirme seu e-mail antes de fazer login");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário inativo");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("E-mail ou senha inválidos");
        }

        return tokenServicePort.generateToken(user);
    }
}

