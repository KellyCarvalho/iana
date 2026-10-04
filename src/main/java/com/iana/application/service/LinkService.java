package com.iana.application.service;

import com.iana.domain.event.LinkCreatedEvent;
import com.iana.domain.model.Link;
import com.iana.domain.ports.in.CreateLinkUseCase;
import com.iana.domain.ports.in.FindLinkUseCase;
import com.iana.domain.ports.out.BookLinkCheckerPort;
import com.iana.domain.ports.out.EventPublisherPort;
import com.iana.domain.ports.out.LinkRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LinkService implements CreateLinkUseCase, FindLinkUseCase {

    private final LinkRepositoryPort linkRepositoryPort;
    private final BookLinkCheckerPort bookLinkCheckerPort;
    private final EventPublisherPort eventPublisherPort;

    public LinkService(LinkRepositoryPort linkRepositoryPort,
                       BookLinkCheckerPort bookLinkCheckerPort,
                       EventPublisherPort eventPublisherPort) {
        this.linkRepositoryPort = linkRepositoryPort;
        this.bookLinkCheckerPort = bookLinkCheckerPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public Link execute(Link link) {
        if (link.getCreatedAt() == null) {
            link.setCreatedAt(LocalDateTime.now());
        }
        boolean isAccessible = bookLinkCheckerPort.isValidUrl(link.getUrl());
        link.setActive(isAccessible);
        Link savedLink = linkRepositoryPort.save(link);

        // Disparo de evento de domínio
        if (eventPublisherPort != null) {
            eventPublisherPort.publishLinkCreated(new LinkCreatedEvent(
                    savedLink.getId(),
                    savedLink.getUrl(),
                    savedLink.getBookId(),
                    savedLink.getCategoryId(),
                    savedLink.isActive()
            ));
        }

        return savedLink;
    }



    @Override
    public Optional<Link> findById(UUID id) {
        return linkRepositoryPort.findById(id);
    }

    @Override
    public List<Link> findAll() {
        return linkRepositoryPort.findAll();
    }

    @Override
    public List<Link> findByBookId(UUID bookId) {
        return linkRepositoryPort.findByBookId(bookId);
    }

    @Override
    public List<Link> findByCategoryId(UUID categoryId) {
        return linkRepositoryPort.findByCategoryId(categoryId);
    }
}
