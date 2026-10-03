package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.event.NotificationEvent;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationConsumer(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "notifications", groupId = "bookhive-notifications")
    public void consume(NotificationEvent event) {
        userRepository.findById(event.recipientId()).ifPresent(user ->
                notificationService.createNotification(user, event.type(), event.message())
        );
    }
}