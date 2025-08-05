package com.bank.account.service.impl;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.event.AccountEventPublisher;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.CustomerDto;
import com.bank.account.model.entity.Account;
import com.bank.account.model.entity.AccountType;
import com.bank.account.model.entity.CustomerType;
import com.bank.account.model.mapper.AccountMapper;
import com.bank.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.bank.account.exception.BusinessErrors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountEventPublisher eventPublisher;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @InjectMocks
    private AccountServiceImpl accountService;

    private CustomerDto activeCustomer;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        activeCustomer = new CustomerDto(1L, CustomerType.CORPORATE, "ACTIVE");
        accountDto = new AccountDto();
        accountDto.setCustomerId(1L);
        accountDto.setType(AccountType.SAVINGS);
        accountDto.setBalance(500.0);
    }

    @Test
    void whenCreateAccount_withValidData_shouldSucceed() {
        when(customerServiceClient.getCustomerById(1L)).thenReturn(activeCustomer);
        when(accountRepository.countByCustomerId(1L)).thenReturn(0L);
        when(accountMapper.toEntity(any(AccountDto.class))).thenReturn(new Account());
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(accountMapper.toDto(any(Account.class))).thenReturn(new AccountDto());

        accountService.createAccount(accountDto);

        verify(eventPublisher).publishAccountCreatedEvent(any(AccountDto.class));
    }

    @Test
    void whenCreateAccount_forInactiveCustomer_shouldThrowException() {
        activeCustomer = new CustomerDto(1L, CustomerType.CORPORATE, "INACTIVE");
        when(customerServiceClient.getCustomerById(1L)).thenReturn(activeCustomer);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getBusinessError()).isEqualTo(CUSTOMER_INACTIVE);
    }

    @Test
    void whenCreateAccount_exceedingMaxAccounts_shouldThrowException() {
        when(customerServiceClient.getCustomerById(1L)).thenReturn(activeCustomer);
        when(accountRepository.countByCustomerId(1L)).thenReturn(10L);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getBusinessError()).isEqualTo(ACCOUNT_LIMIT_EXCEEDED);
    }

    @Test
    void whenCreateSalaryAccount_alreadyExists_shouldThrowException() {
        accountDto.setType(AccountType.SALARY);
        when(customerServiceClient.getCustomerById(1L)).thenReturn(activeCustomer);
        when(accountRepository.findByCustomerIdAndType(1L, AccountType.SALARY)).thenReturn(Optional.of(new Account()));

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getBusinessError()).isEqualTo(SALARY_ACCOUNT_ALREADY_EXISTS);
    }

    @Test
    void whenCreateInvestmentAccount_withInsufficientBalance_shouldThrowException() {
        accountDto.setType(AccountType.INVESTMENT);
        accountDto.setBalance(9000.0);
        when(customerServiceClient.getCustomerById(1L)).thenReturn(activeCustomer);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getBusinessError()).isEqualTo(INVESTMENT_ACCOUNT_MIN_BALANCE);
    }

    @Test
    void whenCreateAccount_forRetailCustomer_withInvalidType_shouldThrowException() {
        activeCustomer = new CustomerDto(1L, CustomerType.RETAIL, "ACTIVE");
        accountDto.setType(AccountType.INVESTMENT);
        when(customerServiceClient.getCustomerById(1L)).thenReturn(activeCustomer);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getBusinessError()).isEqualTo(RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID);
    }
}
