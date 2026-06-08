package org.registrationservice.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.registrationservice.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final RabbitTemplate rabbitTemplate;

    @Value("${audit.routing.key}")
    private String auditRoutingKey;

    @Value("${audit.exchange.name}")
    private String auditExchangeName;

    @Before("execution(*.* publish(..))")
    public void logPublish(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String logMessage = String.format("Executing Registration method: %s with arguments: %s",
                methodName, Arrays.toString(args));
        
        log.info(logMessage);

        rabbitTemplate.convertAndSend(
                auditExchangeName,
                auditRoutingKey,
                logMessage
        );
    }
}
