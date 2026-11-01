package com.iana.infrastructure.adapters.out.security;

import com.iana.domain.model.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenAdapterTest {

    private JwtTokenAdapter jwtTokenAdapter;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        String secret = "iana-default-secret-key-with-at-least-32-bytes-length-2026";
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtTokenAdapter = new JwtTokenAdapter(jwtEncoder, 3600L);
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido contendo subject, userId e role")
    void shouldGenerateValidJwtToken() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Kelly", "kelly@iana.com", "hashed", "ROLE_ADMIN", true, LocalDateTime.now());

        String token = jwtTokenAdapter.generateToken(user);

        assertThat(token).isNotBlank();
        Jwt decoded = jwtDecoder.decode(token);
        assertThat(decoded.getSubject()).isEqualTo("kelly@iana.com");
        assertThat(decoded.getClaimAsString("userId")).isEqualTo(userId.toString());
        assertThat(decoded.getClaimAsString("role")).isEqualTo("ROLE_ADMIN");
    }
}
