package com.iana.domain.ports.out;

import com.iana.domain.event.LinkCreatedEvent;

public interface EventPublisherPort {

    void publishLinkCreated(LinkCreatedEvent event);
}
