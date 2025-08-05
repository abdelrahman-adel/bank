package com.bank.account.api.v1;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.config.TestContainersConfiguration;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.AccountStatus;
import com.bank.account.model.dto.AccountType;
import com.bank.account.model.dto.CustomerDto;
import com.bank.account.model.dto.CustomerStatus;
import com.bank.account.model.dto.CustomerType;
import com.bank.account.model.entity.Account;
import com.bank.account.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
class AccountControllerV1IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerServiceClient customerServiceClient;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateAccount_withValidData_shouldSucceed() throws Exception {
        // Arrange
        String legalId = "1234567";
        CustomerDto mockCustomer = createMockCustomer(1L, CustomerStatus.ACTIVE, CustomerType.CORPORATE);
        when(customerServiceClient.getCustomerByLegalId(legalId)).thenReturn(mockCustomer);

        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerLegalId(legalId);
        requestDto.setType(AccountType.SAVINGS);
        requestDto.setBalance(500.0);
        requestDto.setStatus(AccountStatus.ACTIVE);

        // Act
        mockMvc.perform(post("/api/v1/account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.customerLegalId").value(legalId));

        // Assert Database State
        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getCustomerId()).isEqualTo(mockCustomer.getId());
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateAccount_forNonExistentCustomer_shouldReturnNotFound() throws Exception {
        // Arrange
        String legalId = "9999999";
        when(customerServiceClient.getCustomerByLegalId(legalId)).thenReturn(null);

        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerLegalId(legalId);
        requestDto.setType(AccountType.SAVINGS);
        requestDto.setBalance(100.0);
        requestDto.setStatus(AccountStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/v1/account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateAccount_forInactiveCustomer_shouldReturnBadRequest() throws Exception {
        // Arrange
        String legalId = "1112223";
        CustomerDto mockCustomer = createMockCustomer(2L, CustomerStatus.INACTIVE, CustomerType.RETAIL);
        when(customerServiceClient.getCustomerByLegalId(legalId)).thenReturn(mockCustomer);

        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerLegalId(legalId);
        requestDto.setType(AccountType.SAVINGS);
        requestDto.setBalance(200.0);
        requestDto.setStatus(AccountStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/v1/account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAccountById_withExistingAccount_shouldReturnOk() throws Exception {
        // Arrange
        Account account = accountRepository.save(createAccount(1L, "123-ABC"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/account/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.accountNumber").value(account.getAccountNumber()));
    }

    @Test
    @Transactional
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAllAccounts_shouldReturnAccountList() throws Exception {
        // Arrange
        accountRepository.save(createAccount(1L, "101-ABC"));
        accountRepository.save(createAccount(2L, "202-XYZ"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenDeleteAccount_shouldReturnNoContent() throws Exception {
        // Arrange
        Account account = accountRepository.save(createAccount(1L, "303-DEL"));
        long accountId = account.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/account/{id}", accountId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify DB state
        assertThat(accountRepository.findById(accountId)).isNotPresent();
    }

    private CustomerDto createMockCustomer(Long id, CustomerStatus status, CustomerType type) {
        return new CustomerDto(id, type, status);
    }

    private Account createAccount(Long customerId, String accountNumber) {
        Account account = new Account();
        account.setCustomerId(customerId);
        account.setAccountNumber(accountNumber);
        account.setType(AccountType.SAVINGS);
        account.setBalance(1000.0);
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }

    /**
     * This nested class tests failure scenarios by mocking the RabbitTemplate.
     * It runs in a separate Spring context to avoid interfering with tests that need the real RabbitMQ connection.
     */
    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureMockMvc
    @Import(TestContainersConfiguration.class)
    class PublishingFailureIntegrationTest {

        @MockitoBean
        private RabbitTemplate rabbitTemplate;

        @MockitoBean
        private CustomerServiceClient innerCustomerServiceClient;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        void whenCreateAccount_andPublishingFails_shouldRollbackAndReturnError() throws Exception {
            // Arrange: Mock the Feign client to return a valid customer
            String legalId = "9998887";
            CustomerDto mockCustomer = createMockCustomer(1L, CustomerStatus.ACTIVE, CustomerType.CORPORATE);
            when(innerCustomerServiceClient.getCustomerByLegalId(legalId)).thenReturn(mockCustomer);

            // Arrange: Mock the RabbitTemplate to fail
            doThrow(new RuntimeException("RabbitMQ is down!")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AccountDto.class));

            AccountDto requestDto = new AccountDto();
            requestDto.setCustomerLegalId(legalId);
            requestDto.setType(AccountType.SAVINGS);
            requestDto.setBalance(100.0);
            requestDto.setStatus(AccountStatus.ACTIVE);

            // Act & Assert API Response
            mockMvc.perform(post("/api/v1/account")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError());

            // Assert Database State (transaction should have been rolled back)
            assertThat(accountRepository.count()).isZero();
        }
    }
}
