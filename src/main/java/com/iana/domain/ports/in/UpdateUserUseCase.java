package com.iana.domain.ports.in;

import com.iana.domain.model.User;

import java.util.UUID;

public interface UpdateUserUseCase {
    User update(UUID id, User user);
    User updateRole(UUID id, String role);
}

