package com.github.qqrayzqq.cargoflow.dto.user;

import jakarta.validation.constraints.Email;

public record UpdateUserDto(
    String username,
    @Email String email,
    String password,
    String fullName
) {}
