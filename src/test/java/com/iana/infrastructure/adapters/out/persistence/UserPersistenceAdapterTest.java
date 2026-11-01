package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.User;
import com.iana.infrastructure.adapters.out.persistence.entity.UserJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.UserPersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataUserRepository;
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
class UserPersistenceAdapterTest {

    @Mock
    private SpringDataUserRepository repository;

    @Mock
    private UserPersistenceMapper mapper;

    @InjectMocks
    private UserPersistenceAdapter adapter;

    private UUID userId;
    private User user;
    private UserJpaEntity userJpaEntity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User(userId, "Kelly", "kelly@iana.com", "hashed", "ROLE_USER", true, LocalDateTime.now());
        userJpaEntity = new UserJpaEntity(userId, "Kelly", "kelly@iana.com", "hashed", "ROLE_USER", true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve salvar usuário via adapter")
    void shouldSaveUser() {
        when(mapper.toEntity(user)).thenReturn(userJpaEntity);
        when(repository.save(userJpaEntity)).thenReturn(userJpaEntity);
        when(mapper.toDomain(userJpaEntity)).thenReturn(user);

        User saved = adapter.save(user);

        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("kelly@iana.com");
        verify(repository, times(1)).save(userJpaEntity);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID via adapter")
    void shouldFindById() {
        when(repository.findById(userId)).thenReturn(Optional.of(userJpaEntity));
        when(mapper.toDomain(userJpaEntity)).thenReturn(user);

        Optional<User> found = adapter.findById(userId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Deve buscar usuário por e-mail via adapter")
    void shouldFindByEmail() {
        when(repository.findByEmail("kelly@iana.com")).thenReturn(Optional.of(userJpaEntity));
        when(mapper.toDomain(userJpaEntity)).thenReturn(user);

        Optional<User> found = adapter.findByEmail("kelly@iana.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("kelly@iana.com");
    }

    @Test
    @DisplayName("Deve listar todos os usuários via adapter")
    void shouldFindAll() {
        when(repository.findAll()).thenReturn(List.of(userJpaEntity));
        when(mapper.toDomain(userJpaEntity)).thenReturn(user);

        List<User> list = adapter.findAll();

        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar usuário por ID via adapter")
    void shouldDeleteById() {
        doNothing().when(repository).deleteById(userId);

        adapter.deleteById(userId);

        verify(repository, times(1)).deleteById(userId);
    }
}
