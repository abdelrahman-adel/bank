package com.bank.customer.api.v1;

import com.bank.customer.config.SecurityConfig;
import com.bank.customer.exception.BusinessErrors;
import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.model.entity.CustomerType;
import com.bank.customer.service.CustomerService;
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