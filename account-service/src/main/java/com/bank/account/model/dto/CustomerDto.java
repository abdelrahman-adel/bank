package com.bank.account.model.dto;

import com.bank.account.model.entity.CustomerType;

public record CustomerDto(
        Long id,
        CustomerType type,
        String status
) {
}
