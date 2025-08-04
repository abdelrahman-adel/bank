package com.bank.customer.model.dto;

import com.bank.customer.model.entity.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerDto {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^\\d{7}$", message = "Legal ID must be 7 digits.")
    private String legalId;

    @NotNull
    private CustomerType type;

    private String address;
}
