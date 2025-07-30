package com.bank.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoDto {

    private Long id;

    @NotBlank
    private String civilId;

    @NotBlank
    private String name;

    @NotNull
    private LocalDateTime expiryDate;
}
