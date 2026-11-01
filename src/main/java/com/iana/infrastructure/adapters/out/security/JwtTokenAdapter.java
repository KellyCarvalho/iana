package com.iana.infrastructure.adapters.out.security;

import com.iana.domain.model.User;
import com.iana.domain.ports.out.TokenServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenAdapter implements TokenServicePort {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public JwtTokenAdapter(JwtEncoder jwtEncoder,
                           @Value("${jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("iana-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(user.getEmail())
                .claim("userId", user.getId() != null ? user.getId().toString() : "")
                .claim("role", role)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
