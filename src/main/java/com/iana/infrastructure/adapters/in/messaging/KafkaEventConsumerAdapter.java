package com.iana.infrastructure.adapters.in.messaging;

import com.iana.domain.event.BookCreatedEvent;
import com.iana.domain.event.LinkCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventConsumerAdapter {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumerAdapter.class);

    @KafkaListener(topics = "iana.book-created", groupId = "${spring.kafka.consumer.group-id:iana-group}", autoStartup = "${spring.kafka.listener.auto-startup:false}")
    public void consumeBookCreated(BookCreatedEvent event) {
        log.info("[Kafka Consumer] Evento BookCreatedEvent recebido: bookId={}, title={}", event.getBookId(), event.getTitle());
    }

    @KafkaListener(topics = "iana.link-created", groupId = "${spring.kafka.consumer.group-id:iana-group}", autoStartup = "${spring.kafka.listener.auto-startup:false}")
    public void consumeLinkCreated(LinkCreatedEvent event) {
        log.info("[Kafka Consumer] Evento LinkCreatedEvent recebido: linkId={}, url={}, active={}", event.getLinkId(), event.getUrl(), event.isActive());
    }
}
