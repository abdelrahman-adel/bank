package com.bank.account.api.v1;

import com.bank.account.config.SecurityConfig;
import com.bank.account.exception.BusinessErrors;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.entity.AccountType;
import com.bank.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountControllerV1.class)
@Import(SecurityConfig.class)
class AccountControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateAccount_withValidDataAndAdminRole_shouldReturnCreated() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerId(1L);
        requestDto.setType(AccountType.SAVINGS);

        AccountDto responseDto = new AccountDto();
        responseDto.setId(1L);
        responseDto.setCustomerId(1L);
        responseDto.setType(AccountType.SAVINGS);

        when(accountService.createAccount(any(AccountDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/accounts/1"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenCreateAccount_withUserRole_shouldReturnForbidden() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerId(1L);
        requestDto.setType(AccountType.SAVINGS);

        mockMvc.perform(post("/api/v1/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateAccount_withInvalidCustomerId_shouldReturnBadRequest() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerId(null); // Invalid ID
        requestDto.setType(AccountType.SAVINGS);

        mockMvc.perform(post("/api/v1/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAccountById_withExistingId_shouldReturnAccount() throws Exception {
        AccountDto responseDto = new AccountDto();
        responseDto.setId(1L);
        responseDto.setCustomerId(1L);

        when(accountService.getAccount(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/accounts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAccountById_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(accountService.getAccount(anyLong())).thenThrow(BusinessErrors.NO_SUCH_ACCOUNT.exception());

        mockMvc.perform(get("/api/v1/accounts/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAllAccounts_shouldReturnAccountList() throws Exception {
        AccountDto account1 = new AccountDto();
        account1.setId(1L);
        account1.setCustomerId(1L);

        AccountDto account2 = new AccountDto();
        account2.setId(2L);
        account2.setCustomerId(2L);

        when(accountService.getAllAccounts()).thenReturn(List.of(account1, account2));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].customerId").value(1L));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenUpdateAccount_withAdminRole_shouldReturnOk() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setStatus("INACTIVE");

        AccountDto responseDto = new AccountDto();
        responseDto.setId(1L);
        responseDto.setStatus("INACTIVE");

        when(accountService.updateAccount(anyLong(), any(AccountDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/accounts/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenUpdateAccount_withUserRole_shouldReturnForbidden() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setStatus("INACTIVE");

        mockMvc.perform(put("/api/v1/accounts/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenDeleteAccount_withAdminRole_shouldReturnNoContent() throws Exception {
        doNothing().when(accountService).deleteAccount(1L);

        mockMvc.perform(delete("/api/v1/accounts/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenDeleteAccount_withUserRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}


import com.bank.account.config.SecurityConfig;
import com.bank.account.exception.BusinessErrors;
import com.bank.account.model.dto.CustomerDto;
import AccountType;
import com.bank.account.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerControllerV1.class)
@Import(SecurityConfig.class)
class CustomerControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateCustomer_withValidDataAndAdminRole_shouldReturnCreated() throws Exception {
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Test Corp");
        requestDto.setLegalId("1234567");
        requestDto.setType(CustomerType.CORPORATE);

        CustomerDto responseDto = new CustomerDto();
        responseDto.setId(1L);
        responseDto.setName("Test Corp");
        responseDto.setLegalId("1234567");

        when(customerService.createCustomer(any(CustomerDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/customer/1"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenCreateCustomer_withUserRole_shouldReturnForbidden() throws Exception {
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Test Corp");
        requestDto.setLegalId("1234567");
        requestDto.setType(CustomerType.CORPORATE);

        mockMvc.perform(post("/api/v1/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateCustomer_withInvalidLegalId_shouldReturnBadRequest() throws Exception {
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Test Corp");
        requestDto.setLegalId("123"); // Invalid ID
        requestDto.setType(CustomerType.CORPORATE);

        mockMvc.perform(post("/api/v1/customer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetCustomerById_withExistingId_shouldReturnCustomer() throws Exception {
        CustomerDto responseDto = new CustomerDto();
        responseDto.setId(1L);
        responseDto.setName("Test Corp");
        responseDto.setLegalId("1234567");

        when(customerService.getCustomer(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/customer/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Corp"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetCustomerById_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(customerService.getCustomer(anyLong())).thenThrow(BusinessErrors.NO_SUCH_CUSTOMER.exception());

        mockMvc.perform(get("/api/v1/customer/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAllCustomers_shouldReturnCustomerList() throws Exception {
        CustomerDto customer1 = new CustomerDto();
        customer1.setId(1L);
        customer1.setName("Customer One");

        CustomerDto customer2 = new CustomerDto();
        customer2.setId(2L);
        customer2.setName("Customer Two");

        when(customerService.getAllCustomers()).thenReturn(List.of(customer1, customer2));

        mockMvc.perform(get("/api/v1/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Customer One"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenUpdateCustomer_withAdminRole_shouldReturnOk() throws Exception {
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Updated Name");

        CustomerDto responseDto = new CustomerDto();
        responseDto.setId(1L);
        responseDto.setName("Updated Name");

        when(customerService.updateCustomer(anyLong(), any(CustomerDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/customer/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenUpdateCustomer_withUserRole_shouldReturnForbidden() throws Exception {
        CustomerDto requestDto = new CustomerDto();
        requestDto.setName("Updated Name");

        mockMvc.perform(put("/api/v1/customer/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenDeleteCustomer_withAdminRole_shouldReturnNoContent() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/v1/customer/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenDeleteCustomer_withUserRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/customer/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}