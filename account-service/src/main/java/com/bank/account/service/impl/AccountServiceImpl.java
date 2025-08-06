package com.bank.account.service.impl;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.event.AccountEventPublisher;
import com.bank.account.exception.BusinessErrors;
import com.bank.account.exception.SystemException;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.AccountType;
import com.bank.account.model.dto.AccountUpdateRequest;
import com.bank.account.model.dto.CustomerDto;
import com.bank.account.model.dto.CustomerStatus;
import com.bank.account.model.dto.CustomerType;
import com.bank.account.model.entity.Account;
import com.bank.account.model.mapper.AccountMapper;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("Creating account for customer with legal ID: {}", accountDto.getCustomerLegalId());
        CustomerDto customer = getCustomer(accountDto);

        validateCustomer(customer);
        validateAccountCreation(accountDto, customer);

        Account account = accountMapper.toEntity(accountDto);
        account.setCustomerId(customer.getId());
        account.setAccountNumber(generateAccountNumber(accountDto.getCustomerLegalId()));

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with ID: {}", savedAccount.getId());

        AccountDto savedAccountDto = accountMapper.toDto(savedAccount);
        savedAccountDto.setCustomerLegalId(accountDto.getCustomerLegalId());
        eventPublisher.publishAccountCreatedEvent(savedAccountDto);
        return savedAccountDto;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccount(Long id) {
        log.info("Fetching account with ID: {}", id);
        return accountRepository.findById(id)
                .map(accountMapper::toDto)
                .orElseThrow(BusinessErrors.NO_SUCH_ACCOUNT::exception);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountRepository.findAll().stream()
                .map(accountMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AccountDto updateAccount(Long id, AccountUpdateRequest accountUpdateRequest) {
        log.info("Updating account with ID: {}", id);
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(BusinessErrors.NO_SUCH_ACCOUNT::exception);

        accountMapper.updateAccountFromDto(accountUpdateRequest, existingAccount);

        Account updatedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully with ID: {}", updatedAccount.getId());

        AccountDto updatedAccountDto = accountMapper.toDto(updatedAccount);
        eventPublisher.publishAccountUpdatedEvent(updatedAccountDto);
        return updatedAccountDto;
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        log.info("Deleting account with ID: {}", id);
        if (!accountRepository.existsById(id)) {
            throw BusinessErrors.NO_SUCH_ACCOUNT.exception();
        }
        accountRepository.deleteById(id);
        eventPublisher.publishAccountDeletedEvent(id);
        log.info("Account deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteAccountsByCustomerId(Long customerId) {
        log.info("Deleting all accounts for customer ID: {}", customerId);
        List<Account> accountsToDelete = accountRepository.findByCustomerId(customerId);
        if (accountsToDelete.isEmpty()) {
            log.warn("No accounts found for customer ID: {}, nothing to delete.", customerId);
            return;
        }
        accountRepository.deleteAll(accountsToDelete);
        for (Account account : accountsToDelete) {
            eventPublisher.publishAccountDeletedEvent(account.getId());
        }
        log.info("Successfully deleted {} accounts for customer ID: {}", accountsToDelete.size(), customerId);
    }

    @CircuitBreaker(recover = "getCustomerFallback")
    public CustomerDto getCustomer(AccountDto accountDto) {
        CustomerDto customer;
        try {
            customer = customerServiceClient.getCustomerByLegalId(accountDto.getCustomerLegalId());
        } catch (FeignException.NotFound e) {
            log.warn("Customer not found via Feign client for legal ID: {}", accountDto.getCustomerLegalId());
            throw BusinessErrors.CUSTOMER_NOT_FOUND.exception();
        }
        if (customer == null) {
            throw BusinessErrors.CUSTOMER_NOT_FOUND.exception();
        }
        return customer;
    }

    @Recover
    public CustomerDto getCustomerFallback(Throwable ex) {
        throw new SystemException(ex);
    }

    private void validateCustomer(CustomerDto customer) {
        if (!CustomerStatus.ACTIVE.equals(customer.getStatus())) {
            throw BusinessErrors.CUSTOMER_INACTIVE.exception();
        }
    }

    private void validateAccountCreation(AccountDto accountDto, CustomerDto customer) {
        if (accountRepository.countByCustomerId(customer.getId()) >= MAX_ACCOUNTS_PER_CUSTOMER) {
            throw BusinessErrors.ACCOUNT_LIMIT_EXCEEDED.exception();
        }

        if (CustomerType.RETAIL.equals(customer.getType()) && accountDto.getType() != AccountType.SAVINGS) {
            throw BusinessErrors.RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID.exception();
        }

        if (accountDto.getType() == AccountType.SALARY) {
            accountRepository.findByCustomerIdAndType(customer.getId(), AccountType.SALARY)
                    .ifPresent(a -> {
                        throw BusinessErrors.SALARY_ACCOUNT_ALREADY_EXISTS.exception();
                    });
        }

        if (accountDto.getType() == AccountType.INVESTMENT && accountDto.getBalance() < MIN_INVESTMENT_BALANCE) {
            throw BusinessErrors.INVESTMENT_ACCOUNT_MIN_BALANCE.exception();
        }
    }

    private String generateAccountNumber(String legalId) {
        long randomSuffix = ThreadLocalRandom.current().nextLong(100, 1000);
        return String.format("%s%03d", legalId, randomSuffix);
    }
}
