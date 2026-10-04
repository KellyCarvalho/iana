package com.iana.domain.ports.in;

import com.iana.domain.model.User;

public interface CreateUserUseCase {
    User execute(User user);
}
