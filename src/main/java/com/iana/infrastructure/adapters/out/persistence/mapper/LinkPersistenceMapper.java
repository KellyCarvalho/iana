package com.iana.infrastructure.adapters.out.persistence.mapper;

import com.iana.domain.model.Link;
import com.iana.infrastructure.adapters.out.persistence.entity.LinkJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class LinkPersistenceMapper {

    public LinkJpaEntity toEntity(Link domain) {
        if (domain == null) return null;
        return new LinkJpaEntity(
                domain.getId(),
                domain.getUrl(),
                domain.getTitle(),
                domain.getDescription(),
                domain.getBookId(),
                domain.getCategoryId(),
                domain.isActive(),
                domain.getCreatedAt()
        );
    }

    public Link toDomain(LinkJpaEntity entity) {
        if (entity == null) return null;
        return new Link(
                entity.getId(),
                entity.getUrl(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getBookId(),
                entity.getCategoryId(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
