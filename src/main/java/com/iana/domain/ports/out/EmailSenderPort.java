package com.iana.domain.ports.out;

public interface EmailSenderPort {
    void sendVerificationCode(String toEmail, String userName, String verificationCode);
}
