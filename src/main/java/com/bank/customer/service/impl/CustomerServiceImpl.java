package com.bank.customer.service.impl;

import com.bank.customer.event.CustomerEventPublisher;
import com.bank.customer.exception.BusinessErrors;
import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.entity.Customer;
import com.bank.customer.model.mapper.CustomerMapper;
import com.bank.customer.repository.CustomerRepository;
import com.bank.customer.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
