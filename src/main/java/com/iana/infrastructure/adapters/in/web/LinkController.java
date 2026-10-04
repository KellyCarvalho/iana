package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Link;
import com.iana.domain.ports.in.CreateLinkUseCase;
import com.iana.domain.ports.in.FindLinkUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateLinkRequest;
import com.iana.infrastructure.adapters.in.web.dto.LinkResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/links")
public class LinkController {

    private final CreateLinkUseCase createLinkUseCase;
    private final FindLinkUseCase findLinkUseCase;

    public LinkController(CreateLinkUseCase createLinkUseCase, FindLinkUseCase findLinkUseCase) {
        this.createLinkUseCase = createLinkUseCase;
        this.findLinkUseCase = findLinkUseCase;
    }

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        Link link = new Link(null, request.getUrl(), request.getTitle(), request.getDescription(), request.getBookId(), request.getCategoryId(), true, null);
        Link created = createLinkUseCase.execute(link);
        return ResponseEntity.status(HttpStatus.CREATED).body(LinkResponse.fromDomain(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinkResponse> getLinkById(@PathVariable UUID id) {
        return findLinkUseCase.findById(id)
                .map(LinkResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Link não encontrado com o ID: " + id));
    }


    @GetMapping
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
}
