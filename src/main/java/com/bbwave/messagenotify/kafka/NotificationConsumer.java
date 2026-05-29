package com.bbwave.messagenotify.kafka;

import com.bbwave.messagenotify.model.NotificationMessage;
import com.bbwave.messagenotify.service.NotificationDispatcher;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumes notification messages from Kafka and hands them to the dispatcher.
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(NotificationDispatcher dispatcher, ObjectMapper objectMapper) {
        this.dispatcher = dispatcher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${notify.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(@Payload String payload) {
        log.debug("Received Kafka message: {}", payload);
        try {
            NotificationMessage message = objectMapper.readValue(payload, NotificationMessage.class);
            dispatcher.dispatch(message);
        } catch (Exception ex) {
            log.error("Unable to process Kafka message, skipping. Payload: {}", payload, ex);
        }
    }
}
