package org.emailservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.emailservice.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitConfig rabbitConfig;

    @Value("${audit.routing.key}")
    private String auditRoutingKey;

    public void createEmailTemplate(Map<String, Object> registrationEvent) {
        try {
            String auditMessage = String.format("Creating email template for registration event: %s", registrationEvent);
            log.info(auditMessage);

            String emailContent = buildEmailTemplate(registrationEvent);
            System.out.println("========== EMAIL TEMPLATE ==========");
            System.out.println(emailContent);
            System.out.println("====================================");

            log.info("Email template created successfully");
            
            String completionMessage = String.format("Email template created successfully for event: %s", registrationEvent);
            sendAuditLog(completionMessage);

        } catch (Exception e) {
            log.error("Error creating email template", e);
            String errorMessage = String.format("Error creating email template: %s", e.getMessage());
            sendAuditLog(errorMessage);
        }
    }

    private String buildEmailTemplate(Map<String, Object> event) {
        return String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<body style=\"font-family: Arial, sans-serif;\">\n" +
                        "<h2>Welcome!</h2>\n" +
                        "<p>Dear User,</p>\n" +
                        "<p>Thank you for registering with us.</p>\n" +
                        "<p><strong>Registration Details:</strong></p>\n" +
                        "<ul>\n" +
                        "<li>Email: %s</li>\n" +
                        "<li>Username: %s</li>\n" +
                        "<li>Registration Time: %s</li>\n" +
                        "</ul>\n" +
                        "<p>Best regards,<br/>The Team</p>\n" +
                        "</body>\n" +
                        "</html>",
                event.getOrDefault("email", "N/A"),
                event.getOrDefault("username", "N/A"),
                System.currentTimeMillis()
        );
    }

    private void sendAuditLog(String message) {
        try {
            rabbitTemplate.convertAndSend(
                    rabbitConfig.getAuditExchangeName(),
                    auditRoutingKey,
                    message
            );
        } catch (Exception e) {
            log.error("Failed to send audit log", e);
        }
    }
}
