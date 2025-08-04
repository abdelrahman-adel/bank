package com.bank.customer.service;

import com.bank.customer.model.dto.CustomerDto;

public interface CustomerService {

    CustomerDto createCustomer(CustomerDto customerDto);

    CustomerDto getCustomer(Long id);
}
