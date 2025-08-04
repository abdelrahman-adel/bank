package com.bank.customer.api.v1;

import com.bank.customer.config.RabbitMQConfig;
import com.bank.customer.config.TestContainersConfiguration;
import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.entity.Customer;
import com.bank.customer.model.entity.CustomerType;
import com.bank.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        rabbitTemplate.receive(RabbitMQConfig.CUSTOMER_CREATED_QUEUE);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateCustomer_withValidData_shouldSucceedAndPublishEvent() throws Exception {
        // Arrange
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Integration Test Corp");
        requestDto.setLegalId("7777777");
        requestDto.setType(CustomerType.CORPORATE);
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
        // Use receiveAndConvert with a timeout to wait for the message
        Object message = rabbitTemplate.receiveAndConvert(RabbitMQConfig.CUSTOMER_CREATED_QUEUE, TimeUnit.SECONDS.toMillis(5));
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
        existingCustomer.setType(CustomerType.CORPORATE);
        customerRepository.save(existingCustomer);

        // Prepare a request with the same legalId
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Duplicate Corp");
        requestDto.setLegalId("8888888");
        requestDto.setType(CustomerType.INVESTMENT);

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
        requestDto.setType(CustomerType.RETAIL);

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

    private Customer createCustomer(String name, String legalId) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setLegalId(legalId);
        customer.setType(CustomerType.RETAIL);
        customer.setAddress("Test Address");
        return customer;
    }
}