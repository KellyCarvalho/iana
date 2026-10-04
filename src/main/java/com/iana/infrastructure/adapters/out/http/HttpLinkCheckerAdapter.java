package com.iana.infrastructure.adapters.out.http;

import com.iana.domain.ports.out.BookLinkCheckerPort;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpLinkCheckerAdapter implements BookLinkCheckerPort {

    private final HttpClient httpClient;

    public HttpLinkCheckerAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(url);
            HttpRequest requestHead = HttpRequest.newBuilder()
                    .uri(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", "Mozilla/5.0 (IanaLinkChecker/1.0)")
                    .build();

            HttpResponse<Void> response = httpClient.send(requestHead, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();

            // Se for 405 (Method Not Allowed), tentar com GET
            if (statusCode == 405) {
                HttpRequest requestGet = HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .timeout(Duration.ofSeconds(3))
                        .header("User-Agent", "Mozilla/5.0 (IanaLinkChecker/1.0)")
                        .build();
                response = httpClient.send(requestGet, HttpResponse.BodyHandlers.discarding());
                statusCode = response.statusCode();
            }

            return statusCode >= 200 && statusCode < 400;
        } catch (Exception e) {
            return false;
        }
    }
}
