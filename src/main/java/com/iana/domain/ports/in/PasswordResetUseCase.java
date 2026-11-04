package com.iana.domain.ports.in;

public interface PasswordResetUseCase {
    void requestPasswordReset(String email);
    boolean verifyResetCode(String email, String code);
    void resetPassword(String email, String code, String newPassword);
}
