package com.iana.infrastructure.adapters.out.messaging;

import com.iana.domain.event.BookCreatedEvent;
import com.iana.domain.event.LinkCreatedEvent;
import com.iana.domain.ports.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

    public static final String TOPIC_BOOK_CREATED = "iana.book-created";
    public static final String TOPIC_LINK_CREATED = "iana.link-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired(required = false)
    public KafkaEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishBookCreated(BookCreatedEvent event) {
        log.info("Publicando evento BookCreatedEvent no tópico Kafka {}: bookId={}", TOPIC_BOOK_CREATED, event.getBookId());
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(TOPIC_BOOK_CREATED, event.getBookId().toString(), event);
            } catch (Exception e) {
                log.warn("Falha ao publicar evento no Kafka (servidor offline?): {}", e.getMessage());
            }
        }
    }

    @Override
    public void publishLinkCreated(LinkCreatedEvent event) {
        log.info("Publicando evento LinkCreatedEvent no tópico Kafka {}: linkId={}", TOPIC_LINK_CREATED, event.getLinkId());
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(TOPIC_LINK_CREATED, event.getLinkId().toString(), event);
            } catch (Exception e) {
                log.warn("Falha ao publicar evento no Kafka (servidor offline?): {}", e.getMessage());
            }
        }
    }
}
