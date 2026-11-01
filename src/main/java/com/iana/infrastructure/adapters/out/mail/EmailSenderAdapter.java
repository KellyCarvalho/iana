package com.iana.infrastructure.adapters.out.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.ports.out.EmailSenderPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    private final JavaMailSender javaMailSender;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String smtpUsername;
    private final String googleScriptUrl;
    private final String resendApiKey;
    private final String brevoApiKey;
    private final String mailFrom;

    public EmailSenderAdapter(
            @Autowired(required = false) JavaMailSender javaMailSender,
            @Value("${spring.mail.username:${MAIL_USERNAME:}}") String smtpUsername,
            @Value("${mail.google-script-url:${GOOGLE_SCRIPT_EMAIL_URL:}}") String googleScriptUrl,
            @Value("${mail.resend-api-key:${RESEND_API_KEY:}}") String resendApiKey,
            @Value("${mail.brevo-api-key:${BREVO_API_KEY:}}") String brevoApiKey,
            @Value("${mail.from:${MAIL_FROM:}}") String mailFrom) {
        this.javaMailSender = javaMailSender;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.smtpUsername = smtpUsername;
        this.googleScriptUrl = googleScriptUrl;
        this.resendApiKey = resendApiKey;
        this.brevoApiKey = brevoApiKey;
        this.mailFrom = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom : smtpUsername;
    }

    @Override
    public void sendVerificationCode(String toEmail, String userName, String verificationCode) {
        String htmlContent = buildHtmlTemplate(userName, verificationCode);

        // 0. Envio 100% gratuito via Google Apps Script (usa seu próprio Gmail via HTTPS sem bloqueio de porta)
        if (googleScriptUrl != null && !googleScriptUrl.isBlank()) {
            try {
                Map<String, Object> payload = Map.of(
                        "to", toEmail,
                        "subject", "IANA - Seu código de verificação: " + verificationCode,
                        "htmlBody", htmlContent
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(googleScriptUrl.trim()))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("[Email Service] Código enviado via Google Apps Script para {} (status {})", toEmail, response.statusCode());
                return;
            } catch (Exception e) {
                log.error("[Email Service] Erro ao enviar via Google Apps Script para {}: {}", toEmail, e.getMessage());
            }
        }

        // 1. Envio via SMTP (Gmail / Outlook / Servidor SMTP configurado em MAIL_USERNAME e MAIL_PASSWORD)
        if (javaMailSender != null && smtpUsername != null && !smtpUsername.isBlank()) {
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(mailFrom != null && !mailFrom.isBlank() ? mailFrom : smtpUsername);
                helper.setTo(toEmail);
                helper.setSubject("IANA - Seu código de verificação: " + verificationCode);
                helper.setText(htmlContent, true);
                javaMailSender.send(message);
                log.info("[Email Service] Código de verificação enviado via SMTP com sucesso para: {}", toEmail);
                return;
            } catch (Exception e) {
                log.error("[Email Service] Erro ao enviar via SMTP para {}: {}", toEmail, e.getMessage());
            }
        }

        // 2. Envio via Brevo HTTP API (funciona para qualquer destinatário mesmo sem domínio próprio)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String senderEmail = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom : "no-reply@iana.com";
                Map<String, Object> payload = Map.of(
                        "sender", Map.of("name", "IANA", "email", senderEmail),
                        "to", List.of(Map.of("email", toEmail, "name", userName != null ? userName : toEmail)),
                        "subject", "IANA - Seu código de verificação: " + verificationCode,
                        "htmlContent", htmlContent
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                        .timeout(Duration.ofSeconds(8))
                        .header("api-key", brevoApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("[Email Service] Resposta Brevo (status {}): {}", response.statusCode(), response.body());
                return;
            } catch (Exception e) {
                log.error("[Email Service] Erro ao enviar via Brevo para {}: {}", toEmail, e.getMessage());
            }
        }

        // 3. Envio via Resend HTTP API
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            try {
                String senderEmail = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom : "onboarding@resend.dev";
                Map<String, Object> payload = Map.of(
                        "from", senderEmail,
                        "to", List.of(toEmail),
                        "subject", "IANA - Seu código de verificação: " + verificationCode,
                        "html", htmlContent
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.resend.com/emails"))
                        .timeout(Duration.ofSeconds(8))
                        .header("Authorization", "Bearer " + resendApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("[Email Service] Resposta Resend (status {}): {}", response.statusCode(), response.body());
                return;
            } catch (Exception e) {
                log.error("[Email Service] Erro ao enviar via Resend para {}: {}", toEmail, e.getMessage());
            }
        }

        log.warn("[Email Service] Nenhum servidor de e-mail configurado (defina MAIL_USERNAME/MAIL_PASSWORD ou BREVO_API_KEY). Código gerado para {}: {}", toEmail, verificationCode);
    }

    private String buildHtmlTemplate(String userName, String verificationCode) {
        return String.format(
                "<div style='font-family:sans-serif;max-width:480px;margin:0 auto;padding:24px;border:1px solid #e2e8f0;border-radius:8px;'>" +
                "<h2>Olá, %s!</h2>" +
                "<p>Use o código de 6 dígitos abaixo para validar seu e-mail e ativar sua conta no <strong>IANA</strong>:</p>" +
                "<div style='font-size:28px;font-weight:bold;letter-spacing:6px;padding:16px;background:#f1f5f9;text-align:center;border-radius:6px;'>%s</div>" +
                "</div>",
                userName != null ? userName : "usuário",
                verificationCode
        );
    }
}
