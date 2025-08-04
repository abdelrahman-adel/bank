package com.bank.customer.event;

import com.bank.customer.config.RabbitMQConfig;
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
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CUSTOMER_CREATED_QUEUE, customerDto);
            log.info("Published event to queue '{}' for customer ID {}.", RabbitMQConfig.CUSTOMER_CREATED_QUEUE, customerDto.getId());
        } catch (Exception e) {
            log.error("Failed to publish customer created event for customer ID: {}", customerDto.getId(), e);
        }
    }
}