package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ForbiddenException;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.User;
import com.iana.domain.ports.in.CreateUserUseCase;
import com.iana.domain.ports.in.DeleteUserUseCase;
import com.iana.domain.ports.in.FindUserUseCase;
import com.iana.domain.ports.in.UpdateUserUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateUserRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateUserRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateUserRoleRequest;
import com.iana.infrastructure.adapters.in.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase,
                          FindUserUseCase findUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          DeleteUserUseCase deleteUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.findUserUseCase = findUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo usuário (padrão ROLE_USER)")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
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

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um usuário existente (alteração de role restrita a ROLE_ADMIN)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateUserRequest request,
                                                   Authentication authentication) {
        if (request.getRole() != null && !request.getRole().isBlank() && !isAdmin(authentication)) {
            throw new ForbiddenException("Apenas usuários administradores (ROLE_ADMIN) podem alterar a role de um usuário");
        }

        boolean active = request.getActive() != null ? request.getActive() : true;
        User user = new User(
                id,
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                active,
                null
        );
        user.setRole(request.getRole());
        User updated = updateUserUseCase.update(id, user);
        return ResponseEntity.ok(UserResponse.fromDomain(updated));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Alterar a role de outro usuário (exclusivo para ROLE_ADMIN)")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateUserRoleRequest request,
                                                       Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ForbiddenException("Apenas usuários administradores (ROLE_ADMIN) podem alterar a role de um usuário");
        }
        User updated = updateUserUseCase.updateRole(id, request.getRole());
        return ResponseEntity.ok(UserResponse.fromDomain(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um usuário por ID (exclusivo para ROLE_ADMIN)")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ForbiddenException("Apenas usuários administradores (ROLE_ADMIN) podem excluir um usuário");
        }
        deleteUserUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        boolean hasAdminAuthority = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "SCOPE_ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
        if (hasAdminAuthority) {
            return true;
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String roleClaim = jwt.getClaimAsString("role");
            return "ROLE_ADMIN".equalsIgnoreCase(roleClaim) || "ADMIN".equalsIgnoreCase(roleClaim);
        }
        return false;
    }
}


