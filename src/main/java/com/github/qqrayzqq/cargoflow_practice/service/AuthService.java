package com.github.qqrayzqq.cargoflow_practice.service;

import com.github.qqrayzqq.cargoflow_practice.domain.User;
import com.github.qqrayzqq.cargoflow_practice.domain.enums.UserRole;
import com.github.qqrayzqq.cargoflow_practice.dto.user.LoginDto;
import com.github.qqrayzqq.cargoflow_practice.dto.user.RegisterDto;
import com.github.qqrayzqq.cargoflow_practice.exception.AlreadyExistsException;
import com.github.qqrayzqq.cargoflow_practice.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow_practice.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow_practice.repository.UserRepository;
import com.github.qqrayzqq.cargoflow_practice.security.JwtService;
import com.github.qqrayzqq.cargoflow_practice.security.UserDetailsPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        log.info("User {} logged in", user.getUsername());
        return jwtService.generateToken(new UserDetailsPrincipal(user));
    }

    public String register(RegisterDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new AlreadyExistsException("Username is already taken");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AlreadyExistsException("Email is already taken");
        }

        User newUser = new User(dto.getEmail(), dto.getUsername(), dto.getFullName(),
                passwordEncoder.encode(dto.getPassword()));
        newUser.setRole(UserRole.SHIPPER);

        userRepository.save(newUser);

        log.info("User {} registered", newUser.getUsername());
        return jwtService.generateToken(new UserDetailsPrincipal(newUser));
    }
}
