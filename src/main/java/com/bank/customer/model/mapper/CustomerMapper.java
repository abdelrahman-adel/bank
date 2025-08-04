package com.bank.customer.model.mapper;

import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CustomerDto customerDto);

    CustomerDto toDto(Customer customer);
}
