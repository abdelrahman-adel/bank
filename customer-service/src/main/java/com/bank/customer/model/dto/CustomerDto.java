package com.bank.customer.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CustomerDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4877489888834032262L;

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^\\d{7}$", message = "Legal ID must be 7 digits.")
    private String legalId;

    @NotNull
    private CustomerType type;

    private CustomerStatus status;

    private String address;
}
