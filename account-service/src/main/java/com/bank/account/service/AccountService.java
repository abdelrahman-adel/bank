package com.bank.account.service;

import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.AccountUpdateRequest;

import java.util.List;

public interface AccountService {

    AccountDto createAccount(AccountDto accountDto);

    AccountDto getAccount(Long id);

    List<AccountDto> getAllAccounts();

    AccountDto updateAccount(Long id, AccountUpdateRequest accountUpdateRequest);

    void deleteAccount(Long id);

    void deleteAccountsByCustomerId(Long customerId);
}
