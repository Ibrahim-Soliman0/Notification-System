package org.registrationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.registrationservice.model.UserRegistrationRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.exchange.name}")
    private String notificationExchangeName;

    @Value("${audit.exchange.name}")
    private String auditExchangeName;

    @Value("${audit.routing.key}")
    private String auditRoutingKey;

    public void publish(UserRegistrationRequest request) {

        rabbitTemplate.convertAndSend(
                notificationExchangeName,
                "",
                request
        );

        System.out.println("Published event: " + request);
        
        String auditMessage = String.format("User registered: %s with email: %s", 
                request.name(), request.email());
        sendAuditLog(auditMessage);
    }

    private void sendAuditLog(String message) {
        try {
            rabbitTemplate.convertAndSend(
                    auditExchangeName,
                    auditRoutingKey,
                    message
            );
        } catch (Exception e) {
            log.error("Failed to send audit log", e);
        }
    }
}