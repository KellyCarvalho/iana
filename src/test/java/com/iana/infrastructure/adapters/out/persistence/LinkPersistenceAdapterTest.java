package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Link;
import com.iana.infrastructure.adapters.out.persistence.entity.LinkJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.LinkPersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkPersistenceAdapterTest {

    @Mock
    private SpringDataLinkRepository repository;

    @Mock
    private LinkPersistenceMapper mapper;

    @InjectMocks
    private LinkPersistenceAdapter adapter;

    private UUID linkId;
    private UUID bookId;
    private UUID categoryId;
    private Link link;
    private LinkJpaEntity linkJpaEntity;

    @BeforeEach
    void setUp() {
        linkId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        link = new Link(linkId, "https://example.com", "Example", "Description", bookId, categoryId, true, LocalDateTime.now());
        linkJpaEntity = new LinkJpaEntity(linkId, "https://example.com", "Example", "Description", bookId, categoryId, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve buscar links por ID do livro via adapter")
    void shouldFindByBookId() {
        when(repository.findByBookId(bookId)).thenReturn(List.of(linkJpaEntity));
        when(mapper.toDomain(linkJpaEntity)).thenReturn(link);

        List<Link> links = adapter.findByBookId(bookId);

        assertThat(links).hasSize(1);
        assertThat(links.get(0).getBookId()).isEqualTo(bookId);
    }

    @Test
    @DisplayName("Deve buscar links por ID da categoria via adapter")
    void shouldFindByCategoryId() {
        when(repository.findByCategoryId(categoryId)).thenReturn(List.of(linkJpaEntity));
        when(mapper.toDomain(linkJpaEntity)).thenReturn(link);

        List<Link> links = adapter.findByCategoryId(categoryId);

        assertThat(links).hasSize(1);
        assertThat(links.get(0).getCategoryId()).isEqualTo(categoryId);
    }
}
