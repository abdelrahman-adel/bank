package com.bank.account.api.v1;

import com.bank.account.model.dto.AccountDto;
import com.bank.account.service.AccountService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Controller")
@RestController
@RequestMapping("/api/v1/account")
public class AccountControllerV1 {

    private final AccountService accountService;

    @Operation(description = "Create a new account.")
    @ApiResponse(responseCode = "201", description = "Account created successfully.")
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody @Valid AccountDto accountDto) {
        log.info("Request received to create account for customer with ID: {}", accountDto.getCustomerLegalId());
        AccountDto createdAccount = accountService.createAccount(accountDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(createdAccount);
    }

    @Operation(description = "Get account by ID.")
    @ApiResponse(responseCode = "200", description = "Account info.")
    @ApiResponse(responseCode = "404", description = "Account not found.")
    @GetMapping("/{id}")
    public AccountDto getAccountById(@PathVariable Long id) {
        log.info("Request received to get account by ID: {}", id);
        return accountService.getAccount(id);
    }

    @Operation(description = "Get all accounts.")
    @ApiResponse(responseCode = "200", description = "List of all accounts.")
    @GetMapping
    public List<AccountDto> getAllAccounts() {
        log.info("Request received to get all accounts.");
        return accountService.getAllAccounts();
    }

    @Operation(description = "Update an existing account.")
    @ApiResponse(responseCode = "200", description = "Account updated successfully.")
    @ApiResponse(responseCode = "404", description = "Account not found.")
    @PutMapping("/{id}")
    public AccountDto updateAccount(@PathVariable Long id, @RequestBody @Valid AccountDto accountDto) {
        log.info("Request received to update account with ID: {}", id);
        return accountService.updateAccount(id, accountDto);
    }

    @Operation(description = "Delete a account by ID.")
    @ApiResponse(responseCode = "204", description = "Account deleted successfully.")
    @ApiResponse(responseCode = "404", description = "Account not found.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long id) {
        log.info("Request received to delete account with ID: {}", id);
        accountService.deleteAccount(id);
    }
}
