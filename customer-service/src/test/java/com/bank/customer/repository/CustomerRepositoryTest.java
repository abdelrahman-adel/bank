package com.bank.customer.repository;

import com.bank.customer.config.TestContainersConfiguration;
import com.bank.customer.model.dto.CustomerStatus;
import com.bank.customer.model.entity.Customer;
import com.bank.customer.model.dto.CustomerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestContainersConfiguration.class)
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void whenFindByLegalId_withExistingId_shouldReturnCustomer() {
        // Arrange
        Customer newCustomer = new Customer();
        newCustomer.setName("Test Customer");
        newCustomer.setLegalId("1234567");
        newCustomer.setType(CustomerType.RETAIL);
        newCustomer.setAddress("123 Test Street");
        newCustomer.setStatus(CustomerStatus.ACTIVE);

        entityManager.persistAndFlush(newCustomer);

        // Act
        Optional<Customer> found = customerRepository.findByLegalId("1234567");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(newCustomer.getName());
        assertThat(found.get().getLegalId()).isEqualTo(newCustomer.getLegalId());
    }

    @Test
    void whenFindByLegalId_withNonExistingId_shouldReturnEmpty() {
        // Act
        Optional<Customer> found = customerRepository.findByLegalId("9999999");

        // Assert
        assertThat(found).isNotPresent();
    }
}