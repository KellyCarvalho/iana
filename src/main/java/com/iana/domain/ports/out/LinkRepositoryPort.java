package com.iana.domain.ports.out;

import com.iana.domain.model.Link;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepositoryPort {
    Link save(Link link);
    Optional<Link> findById(UUID id);
    List<Link> findAll();
    List<Link> findByBookId(UUID bookId);
    List<Link> findByCategoryId(UUID categoryId);
    void deleteById(UUID id);
}
