package com.iana.domain.ports.in;

public interface AuthenticateUserUseCase {
    String authenticate(String email, String password);
}
