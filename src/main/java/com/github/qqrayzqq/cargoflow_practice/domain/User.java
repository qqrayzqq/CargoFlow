package com.github.qqrayzqq.cargoflow_practice.domain;

import com.github.qqrayzqq.cargoflow_practice.domain.enums.UserRole;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EqualsAndHashCode.Include
    private Long id;

    private String username;

    private String email;

    private String passwordHash;  // никогда не отдаём в GraphQL напрямую

    private String fullName;

    private UserRole role;

    private boolean isActive;

    private OffsetDateTime createdAt;
}
