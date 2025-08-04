package com.bank.customer.model.dto;

import com.bank.customer.model.entity.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDto {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String legalId;

    @NotNull
    private CustomerType type;

    private String address;
}
