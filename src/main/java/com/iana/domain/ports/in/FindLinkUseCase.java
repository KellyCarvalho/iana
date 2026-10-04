package com.iana.domain.ports.in;

import com.iana.domain.model.Link;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindLinkUseCase {
    Optional<Link> findById(UUID id);
    List<Link> findAll();
    List<Link> findByBookId(UUID bookId);
    List<Link> findByCategoryId(UUID categoryId);
}
