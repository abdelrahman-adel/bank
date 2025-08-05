package com.bank.account.event;

import com.bank.account.config.RabbitMQConfig;
import com.bank.account.exception.SystemException;
import com.bank.account.model.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAccountCreatedEvent(AccountDto accountDto) {
        sendEvent(RabbitMQConfig.ACCOUNT_CREATED_ROUTING_KEY, accountDto, accountDto.getId());
    }

    public void publishAccountUpdatedEvent(AccountDto accountDto) {
        sendEvent(RabbitMQConfig.ACCOUNT_UPDATED_ROUTING_KEY, accountDto, accountDto.getId());
    }

    public void publishAccountDeletedEvent(Long id) {
        sendEvent(RabbitMQConfig.ACCOUNT_DELETED_ROUTING_KEY, id, id);
    }

    private <T> void sendEvent(String routingKey, T message, Object messageId) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNT_EVENTS_TOPIC_EXCHANGE, routingKey, message);
            log.info("Published event to exchange '{}' with routing key '{}' for ID: {}.", RabbitMQConfig.ACCOUNT_EVENTS_TOPIC_EXCHANGE, routingKey, messageId);
        } catch (Exception e) {
            log.error("Failed to publish event to exchange '{}' with routing key '{}' for ID: {}.", RabbitMQConfig.ACCOUNT_EVENTS_TOPIC_EXCHANGE, routingKey, messageId, e);
            throw new SystemException(e);
        }
    }
}
