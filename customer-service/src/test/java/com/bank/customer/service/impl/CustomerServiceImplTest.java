package com.bank.customer.service.impl;

import com.bank.customer.event.CustomerEventPublisher;
import com.bank.customer.exception.BusinessException;
import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.dto.CustomerStatus;
import com.bank.customer.model.dto.CustomerType;
import com.bank.customer.model.entity.Customer;
import com.bank.customer.model.mapper.CustomerMapper;
import com.bank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);

        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertThat(savedCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
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

    @Test
    void whenGetAllCustomers_shouldReturnDtoList() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setId(1L);
        Customer customer2 = new Customer();
        customer2.setId(2L);

        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));
        when(customerMapper.toDto(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            CustomerDto dto = new CustomerDto();
            dto.setId(c.getId());
            return dto;
        });

        // Act
        List<CustomerDto> result = customerService.getAllCustomers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void whenUpdateCustomer_withExistingId_shouldSucceedAndPublishEvent() {
        // Arrange
        long customerId = 1L;
        CustomerDto requestDto = new CustomerDto();
        requestDto.setLegalId("1112223");
        requestDto.setName("Updated Name");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setLegalId("1234567");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByLegalId(requestDto.getLegalId())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(existingCustomer);
        when(customerMapper.toDto(existingCustomer)).thenReturn(new CustomerDto());

        // Act
        customerService.updateCustomer(customerId, requestDto);

        // Assert
        verify(customerRepository).save(existingCustomer);
        verify(eventPublisher).publishCustomerUpdatedEvent(any(CustomerDto.class));
    }

    @Test
    void whenUpdateCustomer_withNonExistentId_shouldThrowException() {
        // Arrange
        long customerId = 99L;
        CustomerDto requestDto = new CustomerDto();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> customerService.updateCustomer(customerId, requestDto));
        verify(eventPublisher, never()).publishCustomerUpdatedEvent(any());
    }

    @Test
    void whenUpdateCustomer_withDuplicateLegalId_shouldThrowException() {
        // Arrange
        long customerId = 1L;
        CustomerDto requestDto = new CustomerDto();
        requestDto.setLegalId("1112223"); // The new, conflicting legal ID

        Customer existingCustomer = new Customer(); // The customer being updated
        existingCustomer.setId(customerId);
        existingCustomer.setLegalId("1234567"); // The original legal ID

        Customer conflictingCustomer = new Customer(); // Another customer that already has the new legal ID
        conflictingCustomer.setId(2L);
        conflictingCustomer.setLegalId("1112223");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByLegalId(requestDto.getLegalId())).thenReturn(Optional.of(conflictingCustomer));

        // Act & Assert
        assertThrows(BusinessException.class, () -> customerService.updateCustomer(customerId, requestDto));
        verify(eventPublisher, never()).publishCustomerUpdatedEvent(any());
    }

    @Test
    void whenDeleteCustomer_withExistingId_shouldSucceedAndPublishEvent() {
        // Arrange
        long customerId = 1L;
        when(customerRepository.existsById(customerId)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(customerId);

        // Act
        customerService.deleteCustomer(customerId);

        // Assert
        verify(customerRepository).deleteById(customerId);
        verify(eventPublisher).publishCustomerDeletedEvent(customerId);
    }

    @Test
    void whenDeleteCustomer_withNonExistentId_shouldThrowException() {
        // Arrange
        long customerId = 99L;
        when(customerRepository.existsById(customerId)).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> customerService.deleteCustomer(customerId));
        verify(eventPublisher, never()).publishCustomerDeletedEvent(any());
    }
}