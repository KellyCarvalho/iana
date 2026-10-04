package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.LinkJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpringDataLinkRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataLinkRepository repository;

    @Test
    @DisplayName("Deve salvar um link com sucesso")
    void shouldSaveLink() {
        LinkJpaEntity link = new LinkJpaEntity(null, "https://spring.io", "Spring Framework", "Site oficial do Spring", UUID.randomUUID(), UUID.randomUUID(), true, LocalDateTime.now());

        LinkJpaEntity saved = repository.save(link);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo("https://spring.io");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar links filtrando por ID do livro")
    void shouldFindLinksByBookId() {
        UUID bookId1 = UUID.randomUUID();
        UUID bookId2 = UUID.randomUUID();

        entityManager.persist(new LinkJpaEntity(null, "https://link1.com", "Link 1", "Desc 1", bookId1, null, true, LocalDateTime.now()));
        entityManager.persist(new LinkJpaEntity(null, "https://link2.com", "Link 2", "Desc 2", bookId1, null, true, LocalDateTime.now()));
        entityManager.persist(new LinkJpaEntity(null, "https://link3.com", "Link 3", "Desc 3", bookId2, null, true, LocalDateTime.now()));
        entityManager.flush();

        List<LinkJpaEntity> linksBook1 = repository.findByBookId(bookId1);

        assertThat(linksBook1).hasSize(2);
        assertThat(linksBook1).extracting(LinkJpaEntity::getUrl).containsExactlyInAnyOrder("https://link1.com", "https://link2.com");
    }

    @Test
    @DisplayName("Deve buscar links filtrando por ID da categoria")
    void shouldFindLinksByCategoryId() {
        UUID categoryId = UUID.randomUUID();

        entityManager.persist(new LinkJpaEntity(null, "https://java.com", "Java", "Desc Java", null, categoryId, true, LocalDateTime.now()));
        entityManager.flush();

        List<LinkJpaEntity> linksCategory = repository.findByCategoryId(categoryId);

        assertThat(linksCategory).hasSize(1);
        assertThat(linksCategory.get(0).getTitle()).isEqualTo("Java");
    }
}
