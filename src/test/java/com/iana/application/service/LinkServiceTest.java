package com.iana.application.service;

import com.iana.domain.event.LinkCreatedEvent;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Link;
import com.iana.domain.ports.out.BookLinkCheckerPort;
import com.iana.domain.ports.out.EventPublisherPort;
import com.iana.domain.ports.out.LinkRepositoryPort;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepositoryPort linkRepositoryPort;

    @Mock
    private BookLinkCheckerPort bookLinkCheckerPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private LinkService linkService;

    private UUID linkId;
    private UUID bookId;
    private UUID categoryId;
    private Link link;

    @BeforeEach
    void setUp() {
        linkId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        link = new Link(linkId, "https://spring.io", "Spring", "Documentação Spring", bookId, categoryId, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar link, verificar URL acessível e publicar LinkCreatedEvent")
    void shouldCreateLinkAndPublishEvent() {
        when(bookLinkCheckerPort.isValidUrl("https://spring.io")).thenReturn(true);
        when(linkRepositoryPort.save(link)).thenReturn(link);

        Link saved = linkService.execute(link);

        assertThat(saved.isActive()).isTrue();
        verify(bookLinkCheckerPort, times(1)).isValidUrl("https://spring.io");
        verify(eventPublisherPort, times(1)).publishLinkCreated(any(LinkCreatedEvent.class));
    }

    @Test
    @DisplayName("Deve atualizar link e revalidar a URL")
    void shouldUpdateLinkAndRevalidateUrl() {
        Link updateData = new Link(linkId, "https://docs.spring.io", "Spring Docs", "Nova desc", bookId, categoryId, false, null);
        when(linkRepositoryPort.findById(linkId)).thenReturn(Optional.of(link));
        when(bookLinkCheckerPort.isValidUrl("https://docs.spring.io")).thenReturn(true);
        when(linkRepositoryPort.save(any(Link.class))).thenAnswer(inv -> inv.getArgument(0));

        Link updated = linkService.update(linkId, updateData);

        assertThat(updated.getUrl()).isEqualTo("https://docs.spring.io");
        assertThat(updated.getTitle()).isEqualTo("Spring Docs");
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar link inexistente")
    void shouldThrowWhenUpdatingNonExistentLink() {
        when(linkRepositoryPort.findById(linkId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.update(linkId, link))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve remover link existente")
    void shouldDeleteLink() {
        when(linkRepositoryPort.findById(linkId)).thenReturn(Optional.of(link));
        doNothing().when(linkRepositoryPort).deleteById(linkId);

        linkService.deleteById(linkId);

        verify(linkRepositoryPort, times(1)).deleteById(linkId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao remover link inexistente")
    void shouldThrowWhenDeletingNonExistentLink() {
        when(linkRepositoryPort.findById(linkId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.deleteById(linkId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve buscar links por bookId e categoryId")
    void shouldFindLinksByBookIdAndCategoryId() {
        when(linkRepositoryPort.findByBookId(bookId)).thenReturn(List.of(link));
        when(linkRepositoryPort.findByCategoryId(categoryId)).thenReturn(List.of(link));

        assertThat(linkService.findByBookId(bookId)).hasSize(1);
        assertThat(linkService.findByCategoryId(categoryId)).hasSize(1);
    }
}
