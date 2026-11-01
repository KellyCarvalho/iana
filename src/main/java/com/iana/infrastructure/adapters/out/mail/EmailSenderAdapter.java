package com.iana.infrastructure.adapters.out.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.ports.out.EmailSenderPort;
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
import java.util.Map;

@Component
public class EmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderAdapter.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String resendApiKey;
    private final String mailFrom;

    public EmailSenderAdapter(
            @Value("${mail.resend-api-key:${RESEND_API_KEY:}}") String resendApiKey,
            @Value("${mail.from:${MAIL_FROM:onboarding@resend.dev}}") String mailFrom) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.resendApiKey = resendApiKey;
        this.mailFrom = mailFrom;
    }

    @Override
    public void sendVerificationCode(String toEmail, String userName, String verificationCode) {
        log.info("[Email Service] Enviando código de verificação [{}] para o e-mail: {}", verificationCode, toEmail);

        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.info("[Email Service] (Modo Simulação/Log) Para: {} | Assunto: Seu código de verificação IANA é {}", toEmail, verificationCode);
            return;
        }

        try {
            String htmlContent = String.format(
                    "<div style='font-family:sans-serif;max-width:480px;margin:0 auto;padding:24px;border:1px solid #e2e8f0;border-radius:8px;'>" +
                    "<h2>Olá, %s!</h2>" +
                    "<p>Use o código de 6 dígitos abaixo para validar seu e-mail e ativar sua conta no <strong>IANA</strong>:</p>" +
                    "<div style='font-size:28px;font-weight:bold;letter-spacing:6px;padding:16px;background:#f1f5f9;text-align:center;border-radius:6px;'>%s</div>" +
                    "</div>",
                    userName != null ? userName : "usuário",
                    verificationCode
            );

            Map<String, Object> payload = Map.of(
                    "from", mailFrom,
                    "to", List.of(toEmail),
                    "subject", "IANA - Código de Verificação: " + verificationCode,
                    "html", htmlContent
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("[Email Service] Resposta do provedor de e-mail (status {}): {}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.warn("[Email Service] Falha ao enviar e-mail externo para {}: {}", toEmail, e.getMessage());
        }
    }
}
