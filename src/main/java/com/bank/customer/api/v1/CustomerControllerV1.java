package com.bank.customer.api.v1;

import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@Tag(name = "Customer Controller")
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerControllerV1 {

    private final CustomerService customerService;

    @Operation(description = "Create a new customer.")
    @ApiResponse(responseCode = "201", description = "Customer created successfully.")
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody @Valid CustomerDto customerDto) {
        CustomerDto createdCustomer = customerService.createCustomer(customerDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdCustomer.getId()).toUri();
        return ResponseEntity.created(uri).body(createdCustomer);
    }

    @Operation(description = "Get customer by ID.")
    @ApiResponse(responseCode = "200", description = "Customer info.")
    @ApiResponse(responseCode = "404", description = "Customer not found.")
    @GetMapping("/{id}")
    public CustomerDto getCustomerById(@PathVariable Long id) {
        return customerService.getCustomer(id);
    }
}
