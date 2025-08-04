package com.bank.customer.api.v1;

import com.bank.customer.model.dto.CustomerDto;
import com.bank.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
}
