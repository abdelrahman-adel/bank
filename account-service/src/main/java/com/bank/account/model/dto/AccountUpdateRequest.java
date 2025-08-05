package com.bank.account.model.dto;

import lombok.Data;

@Data
public class AccountUpdateRequest {

    private AccountType type;

    private Double balance;

    private AccountStatus status;
}
