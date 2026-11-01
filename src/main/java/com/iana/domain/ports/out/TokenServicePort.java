package com.iana.domain.ports.out;

import com.iana.domain.model.User;

public interface TokenServicePort {
    String generateToken(User user);
}
