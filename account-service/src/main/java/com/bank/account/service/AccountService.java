package com.bank.account.service;

import com.bank.account.model.dto.AccountDto;

import java.util.List;

public interface AccountService {

    AccountDto createAccount(AccountDto accountDto);

    AccountDto getAccount(Long id);

    List<AccountDto> getAllAccounts();

    AccountDto updateAccount(Long id, AccountDto accountDto);

    void deleteAccount(Long id);
}
