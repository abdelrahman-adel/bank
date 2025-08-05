package com.bank.account.repository;

import com.bank.account.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    long countByCustomerId(Long customerId);
}
