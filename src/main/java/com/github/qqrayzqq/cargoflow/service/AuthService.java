package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.domain.enums.UserRole;
import com.github.qqrayzqq.cargoflow.dto.user.LoginDto;
import com.github.qqrayzqq.cargoflow.dto.user.RegisterDto;
import com.github.qqrayzqq.cargoflow.exception.AlreadyExistsException;
import com.github.qqrayzqq.cargoflow.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.UserRepository;
import com.github.qqrayzqq.cargoflow.security.JwtService;
import com.github.qqrayzqq.cargoflow.security.UserDetailsPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        log.info("User {} logged in", user.getUsername());
        return jwtService.generateToken(new UserDetailsPrincipal(user));
    }

    @Transactional
    public String register(RegisterDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new AlreadyExistsException("Username is already taken");
        }
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new AlreadyExistsException("Email is already taken");
        }

        User newUser = new User(dto.email(), dto.username(), dto.fullName(),
                passwordEncoder.encode(dto.password()));
        newUser.setRole(UserRole.SHIPPER);

        userRepository.save(newUser);

        log.info("User {} registered", newUser.getUsername());
        return jwtService.generateToken(new UserDetailsPrincipal(newUser));
    }
}
