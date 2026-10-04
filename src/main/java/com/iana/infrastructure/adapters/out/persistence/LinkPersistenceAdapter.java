package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Link;
import com.iana.domain.ports.out.LinkRepositoryPort;
import com.iana.infrastructure.adapters.out.persistence.entity.LinkJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.LinkPersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataLinkRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LinkPersistenceAdapter implements LinkRepositoryPort {

    private final SpringDataLinkRepository repository;
    private final LinkPersistenceMapper mapper;

    public LinkPersistenceAdapter(SpringDataLinkRepository repository, LinkPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Link save(Link link) {
        LinkJpaEntity entity = mapper.toEntity(link);
        LinkJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Link> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Link> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Link> findByBookId(UUID bookId) {
        return repository.findByBookId(bookId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Link> findByCategoryId(UUID categoryId) {
        return repository.findByCategoryId(categoryId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
