package com.bank.account.service.impl;

import com.bank.account.event.AccountEventPublisher;
import com.bank.account.exception.BusinessErrors;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.entity.Account;
import com.bank.account.model.mapper.AccountMapper;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountEventPublisher eventPublisher;

    @Override
    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        if (accountRepository.countByCustomerId(accountDto.getCustomerId()) >= 10) {
            throw BusinessErrors.MAX_ACCOUNTS_REACHED.exception();
        }

        Account account = accountMapper.toEntity(accountDto);
        account = accountRepository.save(account);
        log.info("Account for customer ID {} created successfully with ID {}.", account.getCustomerId(), account.getId());

        AccountDto createdDto = accountMapper.toDto(account);
        eventPublisher.publishAccountCreatedEvent(createdDto);
        return createdDto;
    }

    @Override
    public AccountDto getAccount(Long id) {
        return accountRepository.findById(id)
                .map(accountMapper::toDto)
                .orElseThrow(BusinessErrors.NO_SUCH_ACCOUNT::exception);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        log.debug("Fetching all accounts from the database.");
        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AccountDto updateAccount(Long id, AccountDto accountDto) {
        Account accountToUpdate = accountRepository.findById(id)
                .orElseThrow(BusinessErrors.NO_SUCH_ACCOUNT::exception);

        accountMapper.updateAccountFromDto(accountDto, accountToUpdate);
        Account updatedAccount = accountRepository.save(accountToUpdate);
        log.info("Account with ID {} updated successfully.", id);
        accountDto = accountMapper.toDto(updatedAccount);
        eventPublisher.publishAccountUpdatedEvent(accountDto);
        return accountDto;
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw BusinessErrors.NO_SUCH_ACCOUNT.exception();
        }
        accountRepository.deleteById(id);
        eventPublisher.publishAccountDeletedEvent(id);
        log.info("Account with ID {} deleted successfully.", id);
    }
}
