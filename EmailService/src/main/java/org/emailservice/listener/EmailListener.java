package org.emailservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.emailservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailListener {

    private final EmailService emailService;

    @RabbitListener(queues = "${email.service.queue.name}")
    public void consumeRegistrationEvent(Map<String, Object> event) {
        log.info("Received registration event: {}", event);
        emailService.createEmailTemplate(event);
    }
}
