package com.bank.customer.service.impl;

import com.bank.customer.event.CustomerEventPublisher;
import com.bank.customer.exception.BusinessException;
import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.entity.Customer;
import com.bank.customer.model.entity.CustomerType;
import com.bank.customer.model.mapper.CustomerMapper;
import com.bank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void whenCreateCustomer_withUniqueLegalId_shouldSucceedAndPublishEvent() {
        // Arrange
        CustomerDto requestDto = new CustomerDto();
        requestDto.setLegalId("1234567");
        requestDto.setName("New Customer");
        requestDto.setType(CustomerType.RETAIL);

        Customer customerEntity = new Customer();
        Customer savedCustomerEntity = new Customer();
        savedCustomerEntity.setId(1L);

        when(customerRepository.findByLegalId("1234567")).thenReturn(Optional.empty());
        when(customerMapper.toEntity(requestDto)).thenReturn(customerEntity);
        when(customerRepository.save(customerEntity)).thenReturn(savedCustomerEntity);
        when(customerMapper.toDto(savedCustomerEntity)).thenReturn(new CustomerDto());

        // Act
        customerService.createCustomer(requestDto);

        // Assert
        verify(customerRepository).save(customerEntity);
        verify(eventPublisher).publishCustomerCreatedEvent(any(CustomerDto.class));
    }

    @Test
    void whenCreateCustomer_withDuplicateLegalId_shouldThrowException() {
        // Arrange
        CustomerDto requestDto = new CustomerDto();
        requestDto.setLegalId("7654321");

        when(customerRepository.findByLegalId("7654321")).thenReturn(Optional.of(new Customer()));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            customerService.createCustomer(requestDto);
        });

        verify(eventPublisher, never()).publishCustomerCreatedEvent(any(CustomerDto.class));
    }

    @Test
    void whenGetCustomer_withExistingId_shouldReturnDto() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.getCustomer(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetCustomer_withNonExistingId_shouldThrowException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> customerService.getCustomer(99L));
    }
}