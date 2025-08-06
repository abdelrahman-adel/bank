package com.bank.account.event;

import com.bank.account.config.RabbitMQConfig;
import com.bank.account.exception.SystemException;
import com.bank.account.model.dto.AccountDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AccountEventPublisher accountEventPublisher;

    @Test
    void whenPublishAccountCreatedEvent_shouldSendToCorrectRoutingKey() {
        // Arrange
        AccountDto accountDto = new AccountDto();
        accountDto.setId(1L);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AccountDto.class));

        // Act
        accountEventPublisher.publishAccountCreatedEvent(accountDto);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ACCOUNT_EVENTS_TOPIC),
                eq(RabbitMQConfig.ACCOUNT_CREATED_ROUTING_KEY),
                eq(accountDto)
        );
    }

    @Test
    void whenPublishAccountUpdatedEvent_shouldSendToCorrectRoutingKey() {
        // Arrange
        AccountDto accountDto = new AccountDto();
        accountDto.setId(2L);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AccountDto.class));

        // Act
        accountEventPublisher.publishAccountUpdatedEvent(accountDto);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ACCOUNT_EVENTS_TOPIC),
                eq(RabbitMQConfig.ACCOUNT_UPDATED_ROUTING_KEY),
                eq(accountDto)
        );
    }

    @Test
    void whenPublishAccountDeletedEvent_shouldSendToCorrectRoutingKey() {
        // Arrange
        Long accountId = 3L;
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyLong());

        // Act
        accountEventPublisher.publishAccountDeletedEvent(accountId);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ACCOUNT_EVENTS_TOPIC),
                eq(RabbitMQConfig.ACCOUNT_DELETED_ROUTING_KEY),
                eq(accountId)
        );
    }

    @Test
    void whenSendEventFails_shouldThrowSystemException() {
        // Arrange
        AccountDto accountDto = new AccountDto();
        accountDto.setId(4L);
        doThrow(new AmqpException("Connection failed")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AccountDto.class));

        // Act & Assert
        assertThrows(SystemException.class, () -> accountEventPublisher.publishAccountCreatedEvent(accountDto));
    }
}