package com.iana.infrastructure.adapters.out.persistence;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityContextUserProvider {

    private SecurityContextUserProvider() {
    }

    /**
     * Retorna o e-mail do usuário autenticado no contexto de segurança atual,
     * ou null caso não haja usuário autenticado (ex.: testes unitários isolados).
     */
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        return name.trim().toLowerCase();
    }
}
