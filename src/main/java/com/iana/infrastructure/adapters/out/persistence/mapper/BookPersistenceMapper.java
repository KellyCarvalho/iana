package com.iana.infrastructure.adapters.out.persistence.mapper;


import com.iana.domain.model.Book;
import com.iana.infrastructure.adapters.out.persistence.entity.BookJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BookPersistenceMapper {

    public BookJpaEntity toEntity(Book domain) {
        if (domain == null) return null;
        return new BookJpaEntity(
                domain.getTitle(),
                domain.getAuthor(),
                domain.getDescription(),
                domain.getCoverUrl(),
                domain.getCreatedAt()
        );
    }

    public Book toDomain(BookJpaEntity entity) {
        if (entity == null) return null;
        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getDescription(),
                entity.getCoverUrl(),
                entity.getCreatedAt()
        );
    }
}
