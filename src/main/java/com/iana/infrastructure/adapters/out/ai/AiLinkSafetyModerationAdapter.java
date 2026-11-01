package com.iana.infrastructure.adapters.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.exception.BusinessException;
import com.iana.domain.ports.out.LinkSafetyValidatorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class AiLinkSafetyModerationAdapter implements LinkSafetyValidatorPort {

    private static final Logger log = LoggerFactory.getLogger(AiLinkSafetyModerationAdapter.class);

    private static final Pattern DANGEROUS_SCHEME_PATTERN = Pattern.compile(
            "^\\s*(javascript|data|vbscript|file):", Pattern.CASE_INSENSITIVE
    );

    private static final List<String> ILLICIT_KEYWORDS = List.of(
            "porn", "xxx", "xvideos", "pornhub", "redtube", "onlyfans", "nsfw", "hentai",
            "pedofil", "csam", "childabuse",
            "darkweb", "silkroad", ".onion",
            "crack-download", "keygen", "ransomware", "malware", "trojan", "phishing",
            "comprar-drogas", "cocaina", "trafico-de-drogas", "armas-ilegais"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;
    private final String geminiModel;

    public AiLinkSafetyModerationAdapter(
            @Value("${ai.moderation.gemini-api-key:}") String geminiApiKey,
            @Value("${ai.moderation.gemini-model:gemini-2.5-flash}") String geminiModel) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
    }

    @Override
    public void validateSafety(String url, String title, String description) {
        if (url != null && !url.isBlank()) {
            String trimmedUrl = url.trim();

            if (DANGEROUS_SCHEME_PATTERN.matcher(trimmedUrl).find()) {
                throw new BusinessException("Link bloqueado por segurança: protocolo de URL inválido ou malicioso.");
            }

            String normalizedUrl = trimmedUrl.matches("(?i)^https?://.*") ? trimmedUrl : "https://" + trimmedUrl;
            try {
                URI uri = URI.create(normalizedUrl);
                String scheme = uri.getScheme();
                if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                    throw new BusinessException("Link bloqueado por segurança: apenas links HTTP/HTTPS são permitidos.");
                }
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Formato de URL inválido: " + trimmedUrl);
            }
        }

        // Camada 1: Heurística rápida de palavras-chave e domínios ilícitos
        String combined = ((url != null ? url : "") + " " +
                (title != null ? title : "") + " " +
                (description != null ? description : "")).toLowerCase(Locale.ROOT);

        for (String term : ILLICIT_KEYWORDS) {
            if (combined.contains(term)) {
                throw new BusinessException("Conteúdo ou link bloqueado pela moderação de segurança: detectado termo/domínio ilícito ou impróprio (" + term + ").");
            }
        }

        // Camada 2: Verificação Semântica por IA (Google Gemini API) quando a chave está configurada
        if (geminiApiKey != null && !geminiApiKey.isBlank() && url != null && !url.isBlank()) {
            verifyWithGeminiAi(url, title, description);
        }
    }

    private void verifyWithGeminiAi(String url, String title, String description) {
        try {
            String prompt = String.format(
                    "Você é um moderador de segurança de uma biblioteca digital acadêmica e literária. " +
                    "Avalie se o link e os metadados abaixo apontam para conteúdo ilícito (pornografia, abuso, phishing, malware, drogas ilícitas, violência extrema, golpes ou crimes). " +
                    "Responda APENAS no formato JSON: {\"safe\": true/false, \"reason\": \"motivo se unsafe\"}.\n" +
                    "URL: %s\nTítulo: %s\nDescrição: %s",
                    url,
                    title != null ? title : "",
                    description != null ? description : ""
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + geminiModel + ":generateContent?key=" + geminiApiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode root = objectMapper.readTree(response.body());
                String aiText = root.path("candidates").path(0)
                        .path("content").path("parts").path(0)
                        .path("text").asText("");

                String cleanedJson = aiText.replace("```json", "").replace("```", "").trim();
                if (cleanedJson.startsWith("{")) {
                    JsonNode verdict = objectMapper.readTree(cleanedJson);
                    boolean safe = verdict.path("safe").asBoolean(true);
                    if (!safe) {
                        String reason = verdict.path("reason").asText("Conteúdo classificado como ilícito ou inseguro pela IA.");
                        throw new BusinessException("Link bloqueado pela IA de segurança: " + reason);
                    }
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Falha ao consultar API externa de IA para moderação de link (mantendo validação local): {}", e.getMessage());
        }
    }
}
