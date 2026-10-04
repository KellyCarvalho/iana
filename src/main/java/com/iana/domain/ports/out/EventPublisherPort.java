package com.iana.domain.ports.out;

import com.iana.domain.event.BookCreatedEvent;
import com.iana.domain.event.LinkCreatedEvent;

public interface EventPublisherPort {

    void publishBookCreated(BookCreatedEvent event);

    void publishLinkCreated(LinkCreatedEvent event);
}
