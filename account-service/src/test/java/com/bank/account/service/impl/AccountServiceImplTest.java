package com.bank.account.service.impl;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.event.AccountEventPublisher;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.AccountType;
import com.bank.account.model.dto.CustomerDto;
import com.bank.account.model.dto.CustomerStatus;
import com.bank.account.model.dto.CustomerType;
import com.bank.account.model.entity.Account;
import com.bank.account.model.mapper.AccountMapper;
import com.bank.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.bank.account.exception.BusinessErrors.ACCOUNT_LIMIT_EXCEEDED;
import static com.bank.account.exception.BusinessErrors.CUSTOMER_INACTIVE;
import static com.bank.account.exception.BusinessErrors.INVESTMENT_ACCOUNT_MIN_BALANCE;
import static com.bank.account.exception.BusinessErrors.RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID;
import static com.bank.account.exception.BusinessErrors.SALARY_ACCOUNT_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private final String customerLegalId = "1234567";

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
        activeCustomer = new CustomerDto(1L, CustomerType.CORPORATE, CustomerStatus.ACTIVE);
        accountDto = new AccountDto();
        accountDto.setCustomerLegalId(customerLegalId);
        accountDto.setType(AccountType.SAVINGS);
        accountDto.setBalance(500.0);
    }

    @Test
    void createAccount_shouldSucceed_whenDataIsValid() {
        when(customerServiceClient.getCustomerByLegalId(customerLegalId)).thenReturn(activeCustomer);
        when(accountRepository.countByCustomerId(1L)).thenReturn(0L);
        when(accountMapper.toEntity(any(AccountDto.class))).thenReturn(new Account());
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(accountMapper.toDto(any(Account.class))).thenReturn(new AccountDto());

        accountService.createAccount(accountDto);

        verify(eventPublisher).publishAccountCreatedEvent(any(AccountDto.class));
    }

    @Test
    void createAccount_shouldThrowException_whenCustomerIsInactive() {
        activeCustomer = new CustomerDto(1L, CustomerType.CORPORATE, CustomerStatus.INACTIVE);
        when(customerServiceClient.getCustomerByLegalId(customerLegalId)).thenReturn(activeCustomer);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getStatus()).isEqualTo(CUSTOMER_INACTIVE.getHttpStatus());
        assertThat(exception.getMessage()).isEqualTo(CUSTOMER_INACTIVE.getMessage());
    }

    @Test
    void createAccount_shouldThrowException_whenAccountLimitIsExceeded() {
        when(customerServiceClient.getCustomerByLegalId(customerLegalId)).thenReturn(activeCustomer);
        when(accountRepository.countByCustomerId(1L)).thenReturn(10L);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getStatus()).isEqualTo(ACCOUNT_LIMIT_EXCEEDED.getHttpStatus());
        assertThat(exception.getMessage()).isEqualTo(ACCOUNT_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    void createAccount_shouldThrowException_whenSalaryAccountAlreadyExists() {
        accountDto.setType(AccountType.SALARY);
        when(customerServiceClient.getCustomerByLegalId(customerLegalId)).thenReturn(activeCustomer);
        when(accountRepository.findByCustomerIdAndType(1L, AccountType.SALARY)).thenReturn(Optional.of(new Account()));

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getStatus()).isEqualTo(SALARY_ACCOUNT_ALREADY_EXISTS.getHttpStatus());
        assertThat(exception.getMessage()).isEqualTo(SALARY_ACCOUNT_ALREADY_EXISTS.getMessage());
    }

    @Test
    void createAccount_shouldThrowException_whenInvestmentAccountHasInsufficientBalance() {
        accountDto.setType(AccountType.INVESTMENT);
        accountDto.setBalance(9000.0);
        when(customerServiceClient.getCustomerByLegalId(customerLegalId)).thenReturn(activeCustomer);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getStatus()).isEqualTo(INVESTMENT_ACCOUNT_MIN_BALANCE.getHttpStatus());
        assertThat(exception.getMessage()).isEqualTo(INVESTMENT_ACCOUNT_MIN_BALANCE.getMessage());
    }

    @Test
    void createAccount_shouldThrowException_whenRetailCustomerHasInvalidAccountType() {
        activeCustomer = new CustomerDto(1L, CustomerType.RETAIL, CustomerStatus.ACTIVE);
        accountDto.setType(AccountType.INVESTMENT);
        when(customerServiceClient.getCustomerByLegalId(customerLegalId)).thenReturn(activeCustomer);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountDto));
        assertThat(exception.getStatus()).isEqualTo(RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID.getHttpStatus());
        assertThat(exception.getMessage()).isEqualTo(RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID.getMessage());
    }

    @Test
    void getAccount_shouldReturnAccount_whenAccountExists() {
        Account account = new Account();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(new AccountDto());

        AccountDto result = accountService.getAccount(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getAccount_shouldThrowException_whenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.getAccount(1L));
    }

    @Test
    void getAllAccounts_shouldReturnListOfAccounts() {
        when(accountRepository.findAll()).thenReturn(java.util.List.of(new Account()));
        when(accountMapper.toDto(any(Account.class))).thenReturn(new AccountDto());

        java.util.List<AccountDto> result = accountService.getAllAccounts();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void updateAccount_shouldSucceed_whenDataIsValid() {
        Account existingAccount = new Account();
        existingAccount.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(accountMapper.toDto(any(Account.class))).thenReturn(new AccountDto());

        accountService.updateAccount(1L, accountDto);

        verify(eventPublisher).publishAccountUpdatedEvent(any(AccountDto.class));
    }

    @Test
    void updateAccount_shouldThrowException_whenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> accountService.updateAccount(1L, accountDto));
    }

    @Test
    void deleteAccount_shouldSucceed_whenAccountExists() {
        when(accountRepository.existsById(1L)).thenReturn(true);

        accountService.deleteAccount(1L);

        verify(accountRepository).deleteById(1L);
        verify(eventPublisher).publishAccountDeletedEvent(1L);
    }

    @Test
    void deleteAccount_shouldThrowException_whenAccountDoesNotExist() {
        when(accountRepository.existsById(1L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> accountService.deleteAccount(1L));
    }
}
