package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Book;
import com.iana.domain.ports.out.BookRepositoryPort;
import com.iana.infrastructure.adapters.out.persistence.Repoditory.SpringDataBookRepository;
import com.iana.infrastructure.adapters.out.persistence.entity.BookJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.BookPersistenceMapper;
import org.springframework.stereotype.Component;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BookPersistenceAdapter implements BookRepositoryPort {

    private final SpringDataBookRepository repository;
    private final BookPersistenceMapper mapper;

    public BookPersistenceAdapter(SpringDataBookRepository repository, BookPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(Book book) {
        BookJpaEntity entity = mapper.toEntity(book);
        BookJpaEntity savedEntity = repository.save(entity);
        mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Book> findAll(Pageable pageable) {
        return List.of();
    }

    @Override
    public List<Book> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
