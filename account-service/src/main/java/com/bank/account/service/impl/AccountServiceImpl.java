package com.bank.account.service.impl;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.event.AccountEventPublisher;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.CustomerDto;
import com.bank.account.model.dto.CustomerStatus;
import com.bank.account.model.entity.Account;
import com.bank.account.model.entity.AccountType;
import com.bank.account.model.entity.CustomerType;
import com.bank.account.model.mapper.AccountMapper;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.bank.account.exception.BusinessErrors.ACCOUNT_LIMIT_EXCEEDED;
import static com.bank.account.exception.BusinessErrors.CUSTOMER_INACTIVE;
import static com.bank.account.exception.BusinessErrors.CUSTOMER_NOT_FOUND;
import static com.bank.account.exception.BusinessErrors.INVESTMENT_ACCOUNT_MIN_BALANCE;
import static com.bank.account.exception.BusinessErrors.NO_SUCH_ACCOUNT;
import static com.bank.account.exception.BusinessErrors.RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID;
import static com.bank.account.exception.BusinessErrors.SALARY_ACCOUNT_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final int MAX_ACCOUNTS_PER_CUSTOMER = 10;
    private static final double MIN_INVESTMENT_BALANCE = 10000.0;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountEventPublisher eventPublisher;
    private final CustomerServiceClient customerServiceClient;

    @Override
    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        CustomerDto customer = Optional.ofNullable(customerServiceClient.getCustomerByLegalId(accountDto.getCustomerLegalId()))
                .orElseThrow(CUSTOMER_NOT_FOUND::exception);

        validateCustomer(customer);
        validateAccountCreation(accountDto, customer);

        Account account = accountMapper.toEntity(accountDto);
        account.setAccountNumber(generateAccountNumber(accountDto.getCustomerLegalId()));

        Account savedAccount = accountRepository.save(account);

        AccountDto savedAccountDto = accountMapper.toDto(savedAccount);
        eventPublisher.publishAccountCreatedEvent(savedAccountDto);
        return savedAccountDto;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccount(Long id) {
        return accountRepository.findById(id)
                .map(accountMapper::toDto)
                .orElseThrow(NO_SUCH_ACCOUNT::exception);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AccountDto updateAccount(Long id, AccountDto accountDto) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(NO_SUCH_ACCOUNT::exception);

        accountMapper.updateAccountFromDto(accountDto, existingAccount);

        Account updatedAccount = accountRepository.save(existingAccount);

        AccountDto updatedAccountDto = accountMapper.toDto(updatedAccount);
        eventPublisher.publishAccountUpdatedEvent(updatedAccountDto);
        return updatedAccountDto;
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw NO_SUCH_ACCOUNT.exception();
        }
        accountRepository.deleteById(id);
        eventPublisher.publishAccountDeletedEvent(id);
    }

    private void validateCustomer(CustomerDto customer) {
        if (!CustomerStatus.ACTIVE.equals(customer.status())) {
            throw CUSTOMER_INACTIVE.exception();
        }
    }

    private void validateAccountCreation(AccountDto accountDto, CustomerDto customer) {
        if (accountRepository.countByCustomerId(customer.id()) >= MAX_ACCOUNTS_PER_CUSTOMER) {
            throw ACCOUNT_LIMIT_EXCEEDED.exception();
        }

        if (CustomerType.RETAIL.equals(customer.type()) && accountDto.getType() != AccountType.SAVINGS) {
            throw RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID.exception();
        }

        if (accountDto.getType() == AccountType.SALARY) {
            accountRepository.findByCustomerIdAndType(customer.id(), AccountType.SALARY)
                    .ifPresent(a -> {
                        throw SALARY_ACCOUNT_ALREADY_EXISTS.exception();
                    });
        }

        if (accountDto.getType() == AccountType.INVESTMENT && accountDto.getBalance() < MIN_INVESTMENT_BALANCE) {
            throw INVESTMENT_ACCOUNT_MIN_BALANCE.exception();
        }
    }

    private String generateAccountNumber(String legalId) {
        long randomSuffix = ThreadLocalRandom.current().nextLong(100, 1000);
        return String.format("%s%03d", legalId, randomSuffix);
    }
}
