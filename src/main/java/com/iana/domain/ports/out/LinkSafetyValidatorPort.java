package com.iana.domain.ports.out;

public interface LinkSafetyValidatorPort {
    /**
     * Valida se a URL, título e descrição não contêm links maliciosos ou conteúdo ilícito.
     * Caso seja detectado conteúdo ilícito ou inseguro, lança BusinessException.
     */
    void validateSafety(String url, String title, String description);
}
