package com.github.qqrayzqq.cargoflow.domain;

import com.github.qqrayzqq.cargoflow.domain.enums.UserRole;
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

    private String passwordHash;

    private String fullName;

    private UserRole role;

    private boolean isActive;

    private OffsetDateTime createdAt;

    public User(String email, String username, String fullName, String passwordHash) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
    }
}
