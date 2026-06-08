package org.emailservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${notification.exchange.name}")
    private String notificationExchangeName;

    @Value("${audit.exchange.name}")
    private String auditExchangeName;

    @Value("${email.service.queue.name}")
    private String emailQueueName;

    @Value("${sms.service.queue.name}")
    private String smsQueueName;

    @Value("${audit.service.queue.name}")
    private String auditQueueName;

    @Value("${audit.routing.key}")
    private String auditRoutingKey;

    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange(notificationExchangeName);
    }

    @Bean
    public DirectExchange auditExchange() {
        return new DirectExchange(auditExchangeName);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueueName);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(smsQueueName);
    }

    @Bean
    public Queue auditQueue() {
        return new Queue(auditQueueName);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, FanoutExchange notificationExchange) {

        return BindingBuilder
                .bind(emailQueue)
                .to(notificationExchange);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, FanoutExchange notificationExchange) {

        return BindingBuilder
                .bind(smsQueue)
                .to(notificationExchange);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, DirectExchange auditExchange) {

        return BindingBuilder
                .bind(auditQueue)
                .to(auditExchange)
                .with(auditRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    public String getAuditExchangeName() {
        return auditExchangeName;
    }
}

