package com.bank.account.api.v1;

import com.bank.account.config.SecurityConfig;
import com.bank.account.exception.BusinessErrors;
import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.AccountStatus;
import com.bank.account.model.dto.AccountType;
import com.bank.account.model.dto.AccountUpdateRequest;
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
        requestDto.setCustomerLegalId("1234567");
        requestDto.setType(AccountType.SAVINGS);
        requestDto.setBalance(1000.0);
        requestDto.setStatus(AccountStatus.ACTIVE);

        AccountDto responseDto = new AccountDto();
        responseDto.setId(1L);
        responseDto.setCustomerLegalId("1234567");
        responseDto.setType(AccountType.SAVINGS);

        when(accountService.createAccount(any(AccountDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/account/1"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenCreateAccount_withUserRole_shouldReturnForbidden() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerLegalId("1234567");
        requestDto.setType(AccountType.SAVINGS);

        mockMvc.perform(post("/api/v1/account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenCreateAccount_withInvalidCustomerId_shouldReturnBadRequest() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setCustomerLegalId(null); // Invalid ID
        requestDto.setType(AccountType.SAVINGS);

        mockMvc.perform(post("/api/v1/account")
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
        responseDto.setCustomerLegalId("1234567");

        when(accountService.getAccount(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/account/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerLegalId").value("1234567"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAccountById_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(accountService.getAccount(anyLong())).thenThrow(BusinessErrors.NO_SUCH_ACCOUNT.exception());

        mockMvc.perform(get("/api/v1/account/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenGetAllAccounts_shouldReturnAccountList() throws Exception {
        AccountDto account1 = new AccountDto();
        account1.setId(1L);
        account1.setCustomerLegalId("1234567");

        AccountDto account2 = new AccountDto();
        account2.setId(2L);
        account2.setCustomerLegalId("1234567");

        when(accountService.getAllAccounts()).thenReturn(List.of(account1, account2));

        mockMvc.perform(get("/api/v1/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].customerLegalId").value("1234567"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenUpdateAccount_withAdminRole_shouldReturnOk() throws Exception {
        AccountDto requestDto = new AccountDto();
        requestDto.setStatus(AccountStatus.INACTIVE);

        AccountDto responseDto = new AccountDto();
        responseDto.setId(1L);
        responseDto.setStatus(AccountStatus.INACTIVE);

        when(accountService.updateAccount(anyLong(), any(AccountUpdateRequest.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/account/{id}", 1L)
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
        requestDto.setStatus(AccountStatus.INACTIVE);

        mockMvc.perform(put("/api/v1/account/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenDeleteAccount_withAdminRole_shouldReturnNoContent() throws Exception {
        doNothing().when(accountService).deleteAccount(1L);

        mockMvc.perform(delete("/api/v1/account/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenDeleteAccount_withUserRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/account/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
