package com.bank.account.api.v1;

import com.bank.account.config.RabbitMQConfig;
import com.bank.account.config.TestContainersConfiguration;
import com.bank.account.model.dto.CustomerDto;
import com.bank.account.model.entity.Customer;
import com.bank.account.model.entity.AccountType;
import com.bank.account.repository.CustomerRepository;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Import(TestContainersConfiguration.class)
class CustomerControllerV1IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        // Purge the queue to ensure no messages from previous tests interfere
        rabbitTemplate.receive(RabbitMQConfig.ACCOUNT_SERVICE_CUSTOMER_EVENTS_QUEUE);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateCustomer_withValidData_shouldSucceedAndPublishEvent() throws Exception {
        // Arrange
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Integration Test Corp");
        requestDto.setLegalId("7777777");
        requestDto.setType(AccountType.CORPORATE);
        requestDto.setAddress("123 Test St");

        // Act
        mockMvc.perform(post("/api/v1/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Integration Test Corp"));

        // Assert Database State
        assertThat(customerRepository.findByLegalId("7777777")).isPresent();

        // Assert RabbitMQ Message
        Object message = rabbitTemplate.receiveAndConvert(RabbitMQConfig.ACCOUNT_SERVICE_CUSTOMER_EVENTS_QUEUE, TimeUnit.SECONDS.toMillis(5));
        assertThat(message).isNotNull();
        assertThat(message).isInstanceOf(CustomerDto.class);

        CustomerDto receivedDto = (CustomerDto) message;
        assertThat(receivedDto.getLegalId()).isEqualTo("7777777");
        assertThat(receivedDto.getName()).isEqualTo("Integration Test Corp");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateCustomer_withDuplicateLegalId_shouldReturnConflict() throws Exception {
        // Arrange: Create an initial customer directly in the DB
        Customer existingCustomer = new Customer();
        existingCustomer.setName("Original Corp");
        existingCustomer.setLegalId("8888888");
        existingCustomer.setType(AccountType.CORPORATE);
        customerRepository.save(existingCustomer);

        // Prepare a request with the same legalId
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Duplicate Corp");
        requestDto.setLegalId("8888888");
        requestDto.setType(AccountType.INVESTMENT);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateCustomer_withMissingName_shouldReturnBadRequest() throws Exception {
        // Arrange
        CustomerDto requestDto = new CustomerDto();
        requestDto.setLegalId("3334445");
        requestDto.setName(""); // Invalid blank name
        requestDto.setType(AccountType.RETAIL);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetCustomerById_withExistingCustomer_shouldReturnOk() throws Exception {
        // Arrange
        Customer savedCustomer = customerRepository.save(createCustomer("Get Test", "1112223"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/{id}", savedCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCustomer.getId()))
                .andExpect(jsonPath("$.name").value("Get Test"))
                .andExpect(jsonPath("$.legalId").value("1112223"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetCustomerById_withNonExistentCustomer_shouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetCustomerById_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAllCustomers_shouldReturnCustomerList() throws Exception {
        // Arrange
        customerRepository.saveAll(List.of(
                createCustomer("List Customer 1", "1010101"),
                createCustomer("List Customer 2", "2020202")
        ));

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenUpdateCustomer_withValidData_shouldReturnOk() throws Exception {
        // Arrange
        Customer savedCustomer = customerRepository.save(createCustomer("Update Me", "4445556"));

        CustomerDto updateRequest = new CustomerDto();
        updateRequest.setName("Updated Name");
        updateRequest.setLegalId("4445556"); // Keeping the same legalId
        updateRequest.setType(AccountType.INVESTMENT);
        updateRequest.setAddress("Updated Address");

        // Act & Assert
        mockMvc.perform(put("/api/v1/customer/{id}", savedCustomer.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.type").value(AccountType.INVESTMENT.toString()));

        // Verify DB state
        Customer updatedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertThat(updatedCustomer.getName()).isEqualTo("Updated Name");
        assertThat(updatedCustomer.getAddress()).isEqualTo("Updated Address");

        // Assert RabbitMQ Message
        Object message = rabbitTemplate.receiveAndConvert(RabbitMQConfig.ACCOUNT_SERVICE_CUSTOMER_EVENTS_QUEUE, TimeUnit.SECONDS.toMillis(5));
        assertThat(message).isNotNull();
        assertThat(message).isInstanceOf(CustomerDto.class);

        CustomerDto receivedDto = (CustomerDto) message;
        assertThat(receivedDto.getName()).isEqualTo("Updated Name");
        assertThat(receivedDto.getId()).isEqualTo(savedCustomer.getId());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenUpdateCustomer_withUserRole_shouldReturnForbidden() throws Exception {
        CustomerDto updateRequest = new CustomerDto();
        updateRequest.setName("Updated Name");

        mockMvc.perform(put("/api/v1/customer/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenDeleteCustomer_shouldReturnNoContent() throws Exception {
        // Arrange
        Customer savedCustomer = customerRepository.save(createCustomer("Delete Me", "6667778"));
        long customerId = savedCustomer.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/customer/{id}", customerId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify DB state
        assertThat(customerRepository.findById(customerId)).isNotPresent();

        // Assert RabbitMQ Message
        Object message = rabbitTemplate.receiveAndConvert(RabbitMQConfig.ACCOUNT_SERVICE_CUSTOMER_EVENTS_QUEUE, TimeUnit.SECONDS.toMillis(5));
        assertThat(message).isNotNull();
        assertThat(message).isInstanceOf(Long.class);
        Long receivedId = (Long) message;
        assertThat(receivedId).isEqualTo(customerId);
    }

    private Customer createCustomer(String name, String legalId) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setLegalId(legalId);
        customer.setType(AccountType.RETAIL);
        customer.setAddress("Test Address");
        return customer;
    }

    /**
     * This nested class tests failure scenarios by mocking the RabbitTemplate.
     * It runs in a separate Spring context to avoid interfering with tests that need the real RabbitMQ connection.
     */
    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureMockMvc
    @Transactional
    @Import(TestContainersConfiguration.class)
    class PublishingFailureIntegrationTest {

        @MockitoBean
        private RabbitTemplate rabbitTemplate;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private CustomerRepository customerRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        void whenCreateCustomer_andPublishingFails_shouldRollbackAndReturnError() throws Exception {
            // Arrange: Mock the publisher to fail
            doThrow(new RuntimeException("RabbitMQ is down!")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

            CustomerDto requestDto = new CustomerDto();
            requestDto.setName("Fail Test Corp");
            requestDto.setLegalId("9998887");
            requestDto.setType(AccountType.CORPORATE);

            // Act & Assert API Response
            mockMvc.perform(post("/api/v1/customer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError());

            // Assert Database State (transaction should have been rolled back)
            assertThat(customerRepository.findByLegalId("9998887")).isNotPresent();
        }
    }
}