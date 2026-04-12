package com.github.qqrayzqq.cargoflow.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(
    @Email @NotBlank String email,
    @NotBlank String password
) {}
