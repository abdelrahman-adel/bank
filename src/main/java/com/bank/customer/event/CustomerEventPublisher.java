package com.bank.customer.event;

import com.bank.customer.config.RabbitMQConfig;
import com.bank.customer.exception.SystemException;
import com.bank.customer.model.dto.CustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCustomerCreatedEvent(CustomerDto customerDto) {
        sendEvent(RabbitMQConfig.CUSTOMER_CREATED_ROUTING_KEY, customerDto, customerDto.getId());
    }

    public void publishCustomerUpdatedEvent(CustomerDto customerDto) {
        sendEvent(RabbitMQConfig.CUSTOMER_UPDATED_ROUTING_KEY, customerDto, customerDto.getId());
    }

    public void publishCustomerDeletedEvent(Long id) {
        sendEvent(RabbitMQConfig.CUSTOMER_DELETED_ROUTING_KEY, id, id);
    }

    private <T> void sendEvent(String routingKey, T message, Object messageId) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CUSTOMER_EVENTS_TOPIC_EXCHANGE, routingKey, message);
            log.info("Published event to exchange '{}' with routing key '{}' for ID: {}.", RabbitMQConfig.CUSTOMER_EVENTS_TOPIC_EXCHANGE, routingKey, messageId);
        } catch (Exception e) {
            log.error("Failed to publish event to exchange '{}' with routing key '{}' for ID: {}.", RabbitMQConfig.CUSTOMER_EVENTS_TOPIC_EXCHANGE, routingKey, messageId, e);
            throw new SystemException(e);
        }
    }
}