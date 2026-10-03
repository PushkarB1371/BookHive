package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.enums.NotificationType;
import com.example.BookhiveBackend.event.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);
    private static final String TOPIC = "notifications";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(User recipient, NotificationType type, String message) {
        NotificationEvent event = new NotificationEvent(recipient.getId(), type, message);
        kafkaTemplate.send(TOPIC, recipient.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish notification event: {}", ex.getMessage(), ex);
                    } else {
                        log.info("Published notification event to partition {} offset {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}