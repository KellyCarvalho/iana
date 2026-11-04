package com.iana.infrastructure.adapters.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.out.StatementExtractorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

@Component
public class AiStatementExtractorAdapter implements StatementExtractorPort {

    private static final Logger log = LoggerFactory.getLogger(AiStatementExtractorAdapter.class);

    // Padrão 1: Data numérica (15/10 ou 15/10/2026 ou 2026-10-15) + Estabelecimento + Valor (64,90 ou 1.234,56)
    private static final Pattern NUMERIC_DATE_TX_PATTERN = Pattern.compile(
            "(?m)^\\s*(\\d{2}/\\d{2}(?:/\\d{2,4})?|\\d{4}-\\d{2}-\\d{2})\\s+(.+?)\\s+(?:R\\$\\s*)?(-?(?:\\d{1,3}(?:\\.\\d{3})*|\\d+)[,.]\\d{2})\\s*$"
    );

    // Padrão 2: Data por extenso estilo Nubank/Inter/C6 (ex: "15 OUT Uber *Trip R$ 28,50" ou "03 NOV Netflix 55,90")
    private static final Pattern TEXT_DATE_TX_PATTERN = Pattern.compile(
            "(?im)^\\s*(\\d{1,2}\\s+(?:JAN|FEV|MAR|ABR|MAI|JUN|JUL|AGO|SET|OUT|NOV|DEZ))\\s+(.+?)\\s+(?:R\\$\\s*)?(-?(?:\\d{1,3}(?:\\.\\d{3})*|\\d+)[,.]\\d{2})\\s*$"
    );

    // Padrão 3: Linha digitável de boleto bancário FEBRABAN (47 dígitos com ou sem pontos/espaços)
    private static final Pattern BOLETO_LINHA_DIGITAVEL_PATTERN = Pattern.compile(
            "(\\d{5}[.\\s]?\\d{5}\\s+\\d{5}[.\\s]?\\d{6}\\s+\\d{5}[.\\s]?\\d{6}\\s+\\d\\s+\\d{14}|\\b\\d{47}\\b)"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;
    private final String geminiModel;

    public AiStatementExtractorAdapter(
            @Value("${ai.moderation.gemini-api-key:}") String geminiApiKey,
            @Value("${ai.moderation.gemini-model:gemini-2.5-flash}") String geminiModel) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
    }

    @Override
    public List<Expense> extractFromPdf(byte[] pdfBytes, UUID defaultCategoryId) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return List.of();
        }

        // 1. Se a chave do Gemini estiver configurada, tenta extração multimodal inteligente via IA
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            List<Expense> aiExtracted = extractWithGeminiMultimodal(pdfBytes, defaultCategoryId);
            if (!aiExtracted.isEmpty()) {
                return aiExtracted;
            }
        }

        // 2. Extração nativa local do PDF (descompressão FlateDecode + operadores de texto PDF + heurísticas de fatura/boleto)
        String extractedText = extractTextFromPdfBytes(pdfBytes);
        return parseExpensesFromText(extractedText, defaultCategoryId);
    }

    private List<Expense> extractWithGeminiMultimodal(byte[] pdfBytes, UUID defaultCategoryId) {
        try {
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
            String prompt = "Você é um extrator financeiro especializado em faturas de cartão de crédito e boletos brasileiros. " +
                    "Extraia todas as despesas/compras (ou os dados do boleto, caso seja um boleto único) deste documento PDF. " +
                    "Ignore linhas de 'Pagamento recebido', 'Total da fatura' ou juros informativos. " +
                    "Responda APENAS com um array JSON válido no formato: " +
                    "[{\"name\": \"Nome do estabelecimento ou boleto\", \"amount\": 123.45, \"description\": \"Data da compra, parcela ou detalhes\"}].";

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", "application/pdf",
                                            "data", base64Pdf
                                    ))
                            ))
                    )
            );

            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + geminiModel + ":generateContent?key=" + geminiApiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
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
                int startBracket = cleanedJson.indexOf('[');
                int endBracket = cleanedJson.lastIndexOf(']');
                if (startBracket != -1 && endBracket > startBracket) {
                    JsonNode arrayNode = objectMapper.readTree(cleanedJson.substring(startBracket, endBracket + 1));
                    List<Expense> results = new ArrayList<>();
                    for (JsonNode item : arrayNode) {
                        String name = item.path("name").asText("").trim();
                        double rawAmount = item.path("amount").asDouble(0);
                        String desc = item.path("description").asText("Importado de fatura/boleto PDF").trim();
                        if (!name.isEmpty() && rawAmount > 0) {
                            results.add(new Expense(
                                    null,
                                    name.length() > 150 ? name.substring(0, 150) : name,
                                    BigDecimal.valueOf(rawAmount),
                                    desc.length() > 500 ? desc.substring(0, 500) : desc,
                                    defaultCategoryId,
                                    LocalDateTime.now()
                            ));
                        }
                    }
                    return results;
                }
            }
        } catch (Exception e) {
            log.warn("Extração de PDF via Gemini indisponível, utilizando extrator nativo local: {}", e.getMessage());
        }
        return List.of();
    }

    public List<Expense> parseExpensesFromText(String text, UUID defaultCategoryId) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Expense> expenses = new ArrayList<>();

        // 1. Tenta extrair lançamentos com data numérica (ex: 15/10 Estabelecimento 64,90)
        Matcher numMatcher = NUMERIC_DATE_TX_PATTERN.matcher(text);
        while (numMatcher.find()) {
            addMatchedTransaction(expenses, numMatcher.group(1), numMatcher.group(2), numMatcher.group(3), defaultCategoryId);
        }

        // 2. Tenta extrair lançamentos com data por extenso (ex: 15 OUT Estabelecimento 64,90)
        Matcher textDateMatcher = TEXT_DATE_TX_PATTERN.matcher(text);
        while (textDateMatcher.find()) {
            addMatchedTransaction(expenses, textDateMatcher.group(1), textDateMatcher.group(2), textDateMatcher.group(3), defaultCategoryId);
        }

        // 3. Se não encontrou lista de compras de fatura, verifica se é um Boleto Bancário (linha digitável FEBRABAN)
        if (expenses.isEmpty()) {
            Matcher boletoMatcher = BOLETO_LINHA_DIGITAVEL_PATTERN.matcher(text);
            if (boletoMatcher.find()) {
                String digits = boletoMatcher.group(1).replaceAll("\\D", "");
                if (digits.length() == 47) {
                    String valueStr = digits.substring(37, 47);
                    BigDecimal boletoAmount = new BigDecimal(valueStr).movePointLeft(2);
                    if (boletoAmount.compareTo(BigDecimal.ZERO) > 0) {
                        String bankCode = digits.substring(0, 3);
                        expenses.add(new Expense(
                                null,
                                "Boleto Bancário (Banco " + bankCode + ")",
                                boletoAmount,
                                "Importado de PDF | Linha Digitável: " + digits,
                                defaultCategoryId,
                                LocalDateTime.now()
                        ));
                    }
                }
            }
        }

        return expenses;
    }

    private void addMatchedTransaction(List<Expense> expenses, String dateStr, String rawMerchant, String rawAmount, UUID defaultCategoryId) {
        String merchant = rawMerchant.replaceAll("\\s+", " ").trim();
        String lower = merchant.toLowerCase(Locale.ROOT);
        if (merchant.isEmpty()
                || lower.contains("pagamento recebido")
                || lower.contains("pagamento de fatura")
                || lower.contains("total da fatura")
                || lower.contains("saldo anterior")) {
            return;
        }

        BigDecimal amount = parseAmount(rawAmount);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String name = merchant.length() > 150 ? merchant.substring(0, 150) : merchant;
        String description = "Fatura PDF - Data: " + dateStr.trim();
        expenses.add(new Expense(null, name, amount, description, defaultCategoryId, LocalDateTime.now()));
    }

    public String extractTextFromPdfBytes(byte[] pdfBytes) {
        StringBuilder allText = new StringBuilder();
        String rawLatin1 = new String(pdfBytes, StandardCharsets.ISO_8859_1);

        // Extrai blocos de stream do PDF (comprimidos com FlateDecode ou texto puro)
        int pos = 0;
        while (pos < rawLatin1.length()) {
            int streamStart = rawLatin1.indexOf("stream", pos);
            if (streamStart == -1) break;
            int dataStart = streamStart + 6;
            if (dataStart < rawLatin1.length() && rawLatin1.charAt(dataStart) == '\r') dataStart++;
            if (dataStart < rawLatin1.length() && rawLatin1.charAt(dataStart) == '\n') dataStart++;

            int streamEnd = rawLatin1.indexOf("endstream", dataStart);
            if (streamEnd == -1) break;

            int byteLength = streamEnd - dataStart;
            if (byteLength > 0 && byteLength < 5_000_000) {
                byte[] streamBytes = new byte[byteLength];
                System.arraycopy(pdfBytes, dataStart, streamBytes, 0, byteLength);
                String decompressed = tryInflate(streamBytes);
                if (decompressed != null) {
                    extractPdfStrings(decompressed, allText);
                } else {
                    extractPdfStrings(new String(streamBytes, StandardCharsets.UTF_8), allText);
                }
            }
            pos = streamEnd + 9;
        }

        // Caso o PDF não possua blocos stream explícitos, extrai operadores do conteúdo bruto
        if (allText.length() == 0) {
            extractPdfStrings(new String(pdfBytes, StandardCharsets.UTF_8), allText);
        }
        return allText.toString();
    }

    private String tryInflate(byte[] compressed) {
        for (boolean nowrap : new boolean[]{false, true}) {
            Inflater inflater = new Inflater(nowrap);
            try {
                inflater.setInput(compressed);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                while (!inflater.finished() && !inflater.needsInput()) {
                    int count = inflater.inflate(buffer);
                    if (count == 0) break;
                    bos.write(buffer, 0, count);
                }
                if (bos.size() > 0) {
                    return bos.toString(StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {
            } finally {
                inflater.end();
            }
        }
        return null;
    }

    private void extractPdfStrings(String content, StringBuilder out) {
        // Extrai literais de texto PDF entre parênteses: (Texto da linha) Tj ou [(Texto)] TJ
        Pattern parenPattern = Pattern.compile("\\(([^()\\\\]*(?:\\\\.[^()\\\\]*)*)\\)");
        Matcher matcher = parenPattern.matcher(content);
        boolean foundParen = false;
        while (matcher.find()) {
            foundParen = true;
            String token = matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\(", "(")
                    .replace("\\)", ")");
            out.append(token).append(" ");
            if (token.matches(".*(?:\\d+[,.]\\d{2})\\s*$")) {
                out.append("\n");
            }
        }
        if (!foundParen) {
            out.append(content).append("\n");
        } else {
            out.append("\n");
        }
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.replaceAll("[R$\\s\"']", "").trim();
        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');
        if (lastComma > lastDot) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else if (lastDot > lastComma && lastComma != -1) {
            cleaned = cleaned.replace(",", "");
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
