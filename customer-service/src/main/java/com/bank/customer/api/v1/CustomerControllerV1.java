package com.bank.customer.api.v1;

import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Controller")
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerControllerV1 {

    private final CustomerService customerService;

    @Operation(description = "Create a new customer.")
    @ApiResponse(responseCode = "201", description = "Customer created successfully.")
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody @Valid CustomerDto customerDto) {
        log.info("Request received to create customer with legal ID: {}", customerDto.getLegalId());
        CustomerDto createdCustomer = customerService.createCustomer(customerDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdCustomer.getId()).toUri();
        return ResponseEntity.created(uri).body(createdCustomer);
    }

    @Operation(description = "Get customer by ID.")
    @ApiResponse(responseCode = "200", description = "Customer info.")
    @ApiResponse(responseCode = "404", description = "Customer not found.")
    @GetMapping("/{id}")
    public CustomerDto getCustomerById(@PathVariable Long id) {
        log.info("Request received to get customer by ID: {}", id);
        return customerService.getCustomer(id);
    }

    @Operation(description = "Get customer by ID.")
    @ApiResponse(responseCode = "200", description = "Customer info.")
    @ApiResponse(responseCode = "404", description = "Customer not found.")
    @GetMapping("/search")
    public CustomerDto searchCustomer(@RequestParam String legalId) {
        log.info("Request received to get customer by LegalId: {}", legalId);
        return customerService.getCustomer(legalId);
    }

    @Operation(description = "Get all customers.")
    @ApiResponse(responseCode = "200", description = "List of all customers.")
    @GetMapping
    public List<CustomerDto> getAllCustomers() {
        log.info("Request received to get all customers.");
        return customerService.getAllCustomers();
    }

    @Operation(description = "Update an existing customer.")
    @ApiResponse(responseCode = "200", description = "Customer updated successfully.")
    @ApiResponse(responseCode = "404", description = "Customer not found.")
    @PutMapping("/{id}")
    public CustomerDto updateCustomer(@PathVariable Long id, @RequestBody @Valid CustomerDto customerDto) {
        log.info("Request received to update customer with ID: {}", id);
        return customerService.updateCustomer(id, customerDto);
    }

    @Operation(description = "Delete a customer by ID.")
    @ApiResponse(responseCode = "204", description = "Customer deleted successfully.")
    @ApiResponse(responseCode = "404", description = "Customer not found.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        log.info("Request received to delete customer with ID: {}", id);
        customerService.deleteCustomer(id);
    }
}
