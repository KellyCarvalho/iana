package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.model.User;
import com.iana.domain.ports.in.AuthenticateUserUseCase;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.VerifyEmailUseCase;
import com.iana.infrastructure.adapters.in.web.dto.AuthResponse;
import com.iana.infrastructure.adapters.in.web.dto.CreateUserRequest;
import com.iana.infrastructure.adapters.in.web.dto.LoginRequest;
import com.iana.infrastructure.adapters.in.web.dto.UserResponse;
import com.iana.infrastructure.adapters.in.web.dto.VerifyEmailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints para cadastro, verificação de e-mail e autenticação JWT")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final long expirationSeconds;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase,
                          CreateUserUseCase createUserUseCase,
                          VerifyEmailUseCase verifyEmailUseCase,
                          @Value("${jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.createUserUseCase = createUserUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.expirationSeconds = expirationSeconds;
    }

    @PostMapping("/register")
    @Operation(summary = "Criar um novo usuário e gerar token de validação de e-mail")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        User user = new User(
                null,
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                "ROLE_USER",
                false,
                false,
                null,
                null
        );
        User created = createUserUseCase.execute(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromDomain(created));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Confirmar/validar e-mail do usuário via token (POST)")
    public ResponseEntity<UserResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        User verified = verifyEmailUseCase.verifyEmail(request.getToken());
        return ResponseEntity.ok(UserResponse.fromDomain(verified));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Confirmar/validar e-mail do usuário via link/query param (GET)")
    public ResponseEntity<UserResponse> verifyEmailByQuery(@RequestParam("token") String token) {
        User verified = verifyEmailUseCase.verifyEmail(token);
        return ResponseEntity.ok(UserResponse.fromDomain(verified));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário e obter token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authenticateUserUseCase.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(AuthResponse.of(token, expirationSeconds));
    }
}

