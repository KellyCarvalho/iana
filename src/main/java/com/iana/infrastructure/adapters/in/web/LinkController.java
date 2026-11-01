package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Link;
import com.iana.domain.ports.in.CreateLinkUseCase;
import com.iana.domain.ports.in.DeleteLinkUseCase;
import com.iana.domain.ports.in.FindLinkUseCase;
import com.iana.domain.ports.in.UpdateLinkUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateLinkRequest;
import com.iana.infrastructure.adapters.in.web.dto.LinkResponse;
import com.iana.infrastructure.adapters.in.web.dto.UpdateLinkRequest;
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
@RequestMapping("/api/v1/links")
@Tag(name = "Links", description = "Endpoints para gerenciamento e validação de links")
public class LinkController {

    private final CreateLinkUseCase createLinkUseCase;
    private final FindLinkUseCase findLinkUseCase;
    private final UpdateLinkUseCase updateLinkUseCase;
    private final DeleteLinkUseCase deleteLinkUseCase;

    public LinkController(CreateLinkUseCase createLinkUseCase,
                          FindLinkUseCase findLinkUseCase,
                          UpdateLinkUseCase updateLinkUseCase,
                          DeleteLinkUseCase deleteLinkUseCase) {
        this.createLinkUseCase = createLinkUseCase;
        this.findLinkUseCase = findLinkUseCase;
        this.updateLinkUseCase = updateLinkUseCase;
        this.deleteLinkUseCase = deleteLinkUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo link")
    public ResponseEntity<LinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        Link link = new Link(null, request.getUrl(), request.getTitle(), request.getDescription(), request.getBookId(), request.getCategoryId(), true, null);
        Link created = createLinkUseCase.execute(link);
        return ResponseEntity.status(HttpStatus.CREATED).body(LinkResponse.fromDomain(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar link por ID")
    public ResponseEntity<LinkResponse> getLinkById(@PathVariable UUID id) {
        return findLinkUseCase.findById(id)
                .map(LinkResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Link não encontrado com o ID: " + id));
    }

    @GetMapping
    @Operation(summary = "Listar links (opcionalmente filtrando por bookId ou categoryId)")
    public ResponseEntity<List<LinkResponse>> getLinks(@RequestParam(required = false) UUID bookId,
                                                       @RequestParam(required = false) UUID categoryId) {
        List<Link> links;
        if (bookId != null) {
            links = findLinkUseCase.findByBookId(bookId);
        } else if (categoryId != null) {
            links = findLinkUseCase.findByCategoryId(categoryId);
        } else {
            links = findLinkUseCase.findAll();
        }

        List<LinkResponse> response = links.stream()
                .map(LinkResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um link existente")
    public ResponseEntity<LinkResponse> updateLink(@PathVariable UUID id, @Valid @RequestBody UpdateLinkRequest request) {
        Link link = new Link(id, request.getUrl(), request.getTitle(), request.getDescription(), request.getBookId(), request.getCategoryId(), true, null);
        Link updated = updateLinkUseCase.update(id, link);
        return ResponseEntity.ok(LinkResponse.fromDomain(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um link por ID")
    public ResponseEntity<Void> deleteLink(@PathVariable UUID id) {
        deleteLinkUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

