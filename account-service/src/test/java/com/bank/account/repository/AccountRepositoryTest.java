package com.bank.account.repository;

import com.bank.account.config.TestContainersConfiguration;
import com.bank.account.model.dto.AccountType;
import com.bank.account.model.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestContainersConfiguration.class)
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void whenCountByCustomerId_withExistingAccounts_shouldReturnCorrectCount() {
        // Arrange
        Account account1 = new Account();
        account1.setCustomerId(1L);
        account1.setType(AccountType.SAVINGS);
        account1.setBalance(1000.0);
        account1.setStatus("ACTIVE");
        entityManager.persist(account1);

        Account account2 = new Account();
        account2.setCustomerId(1L);
        account2.setType(AccountType.INVESTMENT);
        account2.setBalance(5000.0);
        account2.setStatus("ACTIVE");
        entityManager.persist(account2);

        entityManager.flush();

        // Act
        long count = accountRepository.countByCustomerId(1L);

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void whenCountByCustomerId_withNoAccounts_shouldReturnZero() {
        // Act
        long count = accountRepository.countByCustomerId(2L);

        // Assert
        assertThat(count).isZero();
    }
}
