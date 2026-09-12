package com.github.qqrayzqq.cargoflow.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(
    String username,
    @Email String email,
    @Size(min = 8, max = 20) String password,
    String fullName
) {}
