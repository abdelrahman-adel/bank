package com.bank.account.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountDto {

    private Long id;

    @NotNull(message = "Customer Legal ID cannot be null")
    private String customerLegalId;

    private String accountNumber;

    @NotNull(message = "Account type cannot be null")
    private AccountType type;

    @NotNull(message = "Balance cannot be null")
    private Double balance;

    @NotNull(message = "Status cannot be null")
    private String status;
}
