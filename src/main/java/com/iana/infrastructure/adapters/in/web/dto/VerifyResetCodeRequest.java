package com.iana.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyResetCodeRequest {

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Forneça um e-mail válido")
    private String email;

    @NotBlank(message = "O código de verificação é obrigatório")
    private String code;

    public VerifyResetCodeRequest() {
    }

    public VerifyResetCodeRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
