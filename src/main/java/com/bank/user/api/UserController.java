package com.bank.user.api;

import com.bank.user.model.dto.UserInfoDto;
import com.bank.user.service.UserService;
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
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserInfoDto getUserById(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping(params = "civilId")
    public UserInfoDto getUserByCivilId(@RequestParam String civilId) {
        return userService.getUser(civilId);
    }

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

    @PostMapping
    public ResponseEntity<UserInfoDto> createUser(@RequestBody @Valid UserInfoDto userInfoDto) {
        userInfoDto = userService.createUser(userInfoDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(userInfoDto.getId()).toUri();
        return ResponseEntity.created(uri).body(userInfoDto);
    }

    @PutMapping("/{id}")
    public UserInfoDto updateUser(@PathVariable Long id, @RequestBody @Valid UserInfoDto userInfoDto) {
        return userService.updateUser(id, userInfoDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
