package com.bank.account.service.impl;

import com.bank.account.event.AccountEventPublisher;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.entity.Account;
import com.bank.account.model.entity.AccountType;
import com.bank.account.model.mapper.AccountMapper;
import com.bank.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountEventPublisher eventPublisher;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void whenCreateAccount_withValidData_shouldSucceedAndPublishEvent() {
        // Arrange
        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerId(1L);
        requestDto.setType(AccountType.SAVINGS);

        Account accountEntity = new Account();
        Account savedAccountEntity = new Account();
        savedAccountEntity.setId(1L);

        when(accountRepository.countByCustomerId(1L)).thenReturn(0L);
        when(accountMapper.toEntity(requestDto)).thenReturn(accountEntity);
        when(accountRepository.save(accountEntity)).thenReturn(savedAccountEntity);
        when(accountMapper.toDto(savedAccountEntity)).thenReturn(new AccountDto());

        // Act
        accountService.createAccount(requestDto);

        // Assert
        verify(accountRepository).save(accountEntity);
        verify(eventPublisher).publishAccountCreatedEvent(any(AccountDto.class));
    }

    @Test
    void whenCreateAccount_forCustomerWithMaxAccounts_shouldThrowException() {
        // Arrange
        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerId(1L);

        when(accountRepository.countByCustomerId(1L)).thenReturn(10L);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            accountService.createAccount(requestDto);
        });

        verify(eventPublisher, never()).publishAccountCreatedEvent(any(AccountDto.class));
    }

    @Test
    void whenGetAccount_withExistingId_shouldReturnDto() {
        // Arrange
        Account account = new Account();
        account.setId(1L);
        AccountDto accountDto = new AccountDto();
        accountDto.setId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toDto(account)).thenReturn(accountDto);

        // Act
        AccountDto result = accountService.getAccount(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void whenGetAccount_withNonExistingId_shouldThrowException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> accountService.getAccount(99L));
    }

    @Test
    void whenGetAllAccounts_shouldReturnDtoList() {
        // Arrange
        Account account1 = new Account();
        account1.setId(1L);
        Account account2 = new Account();
        account2.setId(2L);

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
        when(accountMapper.toDto(any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            AccountDto dto = new AccountDto();
            dto.setId(a.getId());
            return dto;
        });

        // Act
        List<AccountDto> result = accountService.getAllAccounts();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void whenUpdateAccount_withExistingId_shouldSucceedAndPublishEvent() {
        // Arrange
        long accountId = 1L;
        AccountDto requestDto = new AccountDto();
        requestDto.setStatus("INACTIVE");

        Account existingAccount = new Account();
        existingAccount.setId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);
        when(accountMapper.toDto(existingAccount)).thenReturn(new AccountDto());

        // Act
        accountService.updateAccount(accountId, requestDto);

        // Assert
        verify(accountRepository).save(existingAccount);
        verify(eventPublisher).publishAccountUpdatedEvent(any(AccountDto.class));
    }

    @Test
    void whenUpdateAccount_withNonExistentId_shouldThrowException() {
        // Arrange
        long accountId = 99L;
        AccountDto requestDto = new AccountDto();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> accountService.updateAccount(accountId, requestDto));
        verify(eventPublisher, never()).publishAccountUpdatedEvent(any());
    }

    @Test
    void whenDeleteAccount_withExistingId_shouldSucceedAndPublishEvent() {
        // Arrange
        long accountId = 1L;
        when(accountRepository.existsById(accountId)).thenReturn(true);
        doNothing().when(accountRepository).deleteById(accountId);

        // Act
        accountService.deleteAccount(accountId);

        // Assert
        verify(accountRepository).deleteById(accountId);
        verify(eventPublisher).publishAccountDeletedEvent(accountId);
    }

    @Test
    void whenDeleteAccount_withNonExistentId_shouldThrowException() {
        // Arrange
        long accountId = 99L;
        when(accountRepository.existsById(accountId)).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> accountService.deleteAccount(accountId));
        verify(eventPublisher, never()).publishAccountDeletedEvent(any());
    }
}
