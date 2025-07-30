package com.bank.user.api.v1;

import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@Tag(name = "User Controller")
@RestController
@RequestMapping("/api/v1/user")
public class UserControllerV1 {

    private final UserService userService;

    @Operation(description = "Get user by ID.")
    @ApiResponse(responseCode = "200", description = "User info.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    @GetMapping("/{id}")
    public UserInfoDto getUserById(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @Operation(description = "Get user by Civil ID.")
    @ApiResponse(responseCode = "200", description = "User info.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    @GetMapping("/search")
    public UserInfoDto getUserByCivilId(@RequestParam String civilId) {
        return userService.getUser(civilId);
    }

    @Operation(description = "Get all users, paginated.")
    @ApiResponse(responseCode = "200", description = "List of User info.")
    @ApiResponse(responseCode = "400", description = "Pagination fields out of range.")
    @GetMapping
    public ResponseEntity<List<UserInfoDto>> getAllUsers(@RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer page,
                                                         @RequestParam(required = false, defaultValue = "10") @Positive Integer pageSize) {
        Page<UserInfoDto> usersPage = userService.getAllUsers(page, pageSize);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(usersPage.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(usersPage.getTotalPages()))
                .header("X-Current-Page", String.valueOf(usersPage.getNumber()))
                .header("X-Page-Size", String.valueOf(usersPage.getSize()))
                .body(usersPage.getContent());
    }

    @Operation(description = "Create a new user.")
    @ApiResponse(responseCode = "200", description = "Created User Info.")
    @ApiResponse(responseCode = "400", description = "Request missing required fields.")
    @ApiResponse(responseCode = "409", description = "User Civil ID is already used.")
    @PostMapping
    public ResponseEntity<UserInfoDto> createUser(@RequestBody @Valid UserInfoDto userInfoDto) {
        userInfoDto = userService.createUser(userInfoDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(userInfoDto.getId()).toUri();
        return ResponseEntity.created(uri).body(userInfoDto);
    }

    @Operation(description = "Update an existing user.")
    @ApiResponse(responseCode = "200", description = "Updated User Info.")
    @ApiResponse(responseCode = "400", description = "Request missing required fields.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    @ApiResponse(responseCode = "409", description = "User Civil ID is already used.")
    @PutMapping("/{id}")
    public UserInfoDto updateUser(@PathVariable Long id, @RequestBody @Valid UserInfoDto userInfoDto) {
        return userService.updateUser(id, userInfoDto);
    }

    @Operation(description = "Delete user by ID.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully.")
    @ApiResponse(responseCode = "404", description = "User not found.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
