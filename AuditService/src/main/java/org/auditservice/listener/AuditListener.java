package org.auditservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.auditservice.model.AuditLog;
import org.auditservice.repository.AuditLogRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditListener {

    private final AuditLogRepository auditLogRepository;

    @RabbitListener(queues = "${audit.service.queue.name}")
    public void consumeAuditMessage(String message) {
        log.info("Received audit message: {}", message);
        
        AuditLog auditLog = new AuditLog();
        auditLog.setMessage(message);
        
        auditLogRepository.save(auditLog);
        
        log.info("Audit log saved with id: {}", auditLog.getId());
    }
}
