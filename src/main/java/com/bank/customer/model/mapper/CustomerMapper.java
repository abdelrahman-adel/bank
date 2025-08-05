package com.bank.customer.model.mapper;

import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {

    Customer toEntity(CustomerDto customerDto);

    CustomerDto toDto(Customer customer);

    void updateCustomerFromDto(CustomerDto dto, @MappingTarget Customer entity);
}
