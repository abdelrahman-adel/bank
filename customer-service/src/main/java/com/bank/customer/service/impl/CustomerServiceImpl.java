package com.bank.customer.service.impl;

import com.bank.customer.event.CustomerEventPublisher;
import com.bank.customer.exception.BusinessErrors;
import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.entity.Customer;
import com.bank.customer.model.entity.CustomerStatus;
import com.bank.customer.model.mapper.CustomerMapper;
import com.bank.customer.repository.CustomerRepository;
import com.bank.customer.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerEventPublisher eventPublisher;

    @Override
    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        customerRepository.findByLegalId(customerDto.getLegalId()).ifPresent(c -> {
            throw BusinessErrors.CUSTOMER_LEGAL_ID_USED.exception();
        });

        Customer customer = customerMapper.toEntity(customerDto);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer = customerRepository.save(customer);
        log.info("Customer with legal ID {} created successfully with ID {}.", customer.getLegalId(), customer.getId());

        CustomerDto createdDto = customerMapper.toDto(customer);
        eventPublisher.publishCustomerCreatedEvent(createdDto);
        return createdDto;
    }

    @Override
    public CustomerDto getCustomer(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDto)
                .orElseThrow(BusinessErrors.NO_SUCH_CUSTOMER::exception);
    }

    @Override
    public CustomerDto getCustomer(String legalId) {
        return customerRepository.findByLegalId(legalId)
                .map(customerMapper::toDto)
                .orElseThrow(BusinessErrors.NO_SUCH_CUSTOMER::exception);
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        log.debug("Fetching all customers from the database.");
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Customer customerToUpdate = customerRepository.findById(id)
                .orElseThrow(BusinessErrors.NO_SUCH_CUSTOMER::exception);

        // Check if the legal ID is being changed to one that already exists for another customer
        if (!Objects.equals(customerToUpdate.getLegalId(), customerDto.getLegalId())) {
            customerRepository.findByLegalId(customerDto.getLegalId()).ifPresent(c -> {
                throw BusinessErrors.CUSTOMER_LEGAL_ID_USED.exception();
            });
        }

        customerMapper.updateCustomerFromDto(customerDto, customerToUpdate);
        Customer updatedCustomer = customerRepository.save(customerToUpdate);
        log.info("Customer with ID {} updated successfully.", id);

        customerDto = customerMapper.toDto(updatedCustomer);
        eventPublisher.publishCustomerUpdatedEvent(customerDto);
        return customerDto;
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw BusinessErrors.NO_SUCH_CUSTOMER.exception();
        }
        customerRepository.deleteById(id);
        eventPublisher.publishCustomerDeletedEvent(id);
        log.info("Customer with ID {} deleted successfully.", id);
    }
}
