package com.iana.infrastructure.adapters.out.ai;

import com.iana.domain.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiLinkSafetyModerationAdapterTest {

    private AiLinkSafetyModerationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AiLinkSafetyModerationAdapter("", "gemini-2.5-flash");
    }

    @Test
    @DisplayName("Deve permitir links legítimos de livros e PDFs")
    void shouldAllowLegitimateBookLinks() {
        assertDoesNotThrow(() -> adapter.validateSafety(
                "https://exemplo.edu.br/arquivos/clean-code.pdf",
                "Clean Code",
                "Livro sobre boas práticas de engenharia de software"
        ));
    }

    @Test
    @DisplayName("Deve bloquear esquemas de URL maliciosos como javascript:")
    void shouldBlockJavascriptScheme() {
        assertThrows(BusinessException.class, () -> adapter.validateSafety(
                "javascript:alert(document.cookie)",
                "Livro",
                "Descrição"
        ));
    }

    @Test
    @DisplayName("Deve bloquear links ou descrições com conteúdo ilícito")
    void shouldBlockIllicitContent() {
        assertThrows(BusinessException.class, () -> adapter.validateSafety(
                "https://site-suspeito.com/phishing-malware.pdf",
                "Arquivo suspeito",
                "Download com ransomware"
        ));
    }
}
