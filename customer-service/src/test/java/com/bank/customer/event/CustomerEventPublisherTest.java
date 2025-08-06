package com.bank.customer.event;

import com.bank.customer.config.RabbitMQConfig;
import com.bank.customer.exception.SystemException;
import com.bank.customer.model.dto.CustomerDto;
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
class CustomerEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CustomerEventPublisher customerEventPublisher;

    @Test
    void whenPublishCustomerCreatedEvent_shouldSendToCorrectRoutingKey() {
        // Arrange
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(1L);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(CustomerDto.class));

        // Act
        customerEventPublisher.publishCustomerCreatedEvent(customerDto);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.CUSTOMER_EVENTS_TOPIC),
                eq(RabbitMQConfig.CUSTOMER_CREATED_ROUTING_KEY),
                eq(customerDto)
        );
    }

    @Test
    void whenPublishCustomerUpdatedEvent_shouldSendToCorrectRoutingKey() {
        // Arrange
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(2L);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(CustomerDto.class));

        // Act
        customerEventPublisher.publishCustomerUpdatedEvent(customerDto);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.CUSTOMER_EVENTS_TOPIC),
                eq(RabbitMQConfig.CUSTOMER_UPDATED_ROUTING_KEY),
                eq(customerDto)
        );
    }

    @Test
    void whenPublishCustomerDeletedEvent_shouldSendToCorrectRoutingKey() {
        // Arrange
        Long customerId = 3L;
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyLong());

        // Act
        customerEventPublisher.publishCustomerDeletedEvent(customerId);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.CUSTOMER_EVENTS_TOPIC),
                eq(RabbitMQConfig.CUSTOMER_DELETED_ROUTING_KEY),
                eq(customerId)
        );
    }

    @Test
    void whenSendEventFails_shouldThrowSystemException() {
        // Arrange
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(4L);
        doThrow(new AmqpException("Connection failed")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(CustomerDto.class));

        // Act & Assert
        assertThrows(SystemException.class, () -> customerEventPublisher.publishCustomerCreatedEvent(customerDto));
    }
}