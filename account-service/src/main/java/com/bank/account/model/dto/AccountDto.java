package com.bank.account.model.dto;

import com.bank.account.model.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AccountDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    private Long customerId;

    @NotNull
    private AccountType type;

    private Double balance;

    private String status;
}
