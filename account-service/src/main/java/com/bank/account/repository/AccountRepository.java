package com.bank.account.repository;

import com.bank.account.model.dto.AccountType;
import com.bank.account.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    long countByCustomerId(Long customerId);

    Optional<Account> findByCustomerIdAndType(Long customerId, AccountType type);
}
