package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpringDataUserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataUserRepository repository;

    @Test
    @DisplayName("Deve salvar um usuário com sucesso")
    void shouldSaveUser() {
        UserJpaEntity user = new UserJpaEntity(null, "Kelly", "kelly@iana.com", "hashed-pass", "ROLE_USER", true, LocalDateTime.now());

        UserJpaEntity saved = repository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Kelly");
        assertThat(saved.getEmail()).isEqualTo("kelly@iana.com");
    }

    @Test
    @DisplayName("Deve buscar usuário por e-mail")
    void shouldFindUserByEmail() {
        UserJpaEntity user = new UserJpaEntity(null, "Kelly", "kelly@iana.com", "hashed-pass", "ROLE_USER", true, LocalDateTime.now());
        entityManager.persistAndFlush(user);

        Optional<UserJpaEntity> found = repository.findByEmail("kelly@iana.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Kelly");
    }

    @Test
    @DisplayName("Deve verificar existência de usuário por e-mail")
    void shouldCheckExistsByEmail() {
        UserJpaEntity user = new UserJpaEntity(null, "Kelly", "kelly@iana.com", "hashed-pass", "ROLE_USER", true, LocalDateTime.now());
        entityManager.persistAndFlush(user);

        assertThat(repository.existsByEmail("kelly@iana.com")).isTrue();
        assertThat(repository.existsByEmail("outro@iana.com")).isFalse();
    }
}
