package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.FindUserUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateUserRequest;
import com.iana.infrastructure.adapters.in.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints para gerenciamento e cadastro de usuários")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final FindUserUseCase findUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase, FindUserUseCase findUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.findUserUseCase = findUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo usuário")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User(
                null,
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                true,
                null
        );

        User createdUser = createUserUseCase.execute(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromDomain(createdUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return findUserUseCase.findById(id)
                .map(UserResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> responseList = findUserUseCase.findAll().stream()
                .map(UserResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }
}
