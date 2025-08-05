package com.bank.customer.service;

import com.bank.customer.model.dto.CustomerDto;

import java.util.List;

public interface CustomerService {

    CustomerDto createCustomer(CustomerDto customerDto);

    CustomerDto getCustomer(Long id);

    CustomerDto getCustomer(String legalId);

    List<CustomerDto> getAllCustomers();

    CustomerDto updateCustomer(Long id, CustomerDto customerDto);

    void deleteCustomer(Long id);
}
