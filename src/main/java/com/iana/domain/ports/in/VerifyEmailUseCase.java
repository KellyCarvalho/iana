package com.iana.domain.ports.in;

import com.iana.domain.model.User;

public interface VerifyEmailUseCase {
    User verifyEmail(String token);
}
