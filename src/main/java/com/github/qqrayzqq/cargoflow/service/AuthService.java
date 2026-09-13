package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.domain.enums.UserRole;
import com.github.qqrayzqq.cargoflow.dto.user.LoginDto;
import com.github.qqrayzqq.cargoflow.dto.user.RegisterDto;
import com.github.qqrayzqq.cargoflow.exception.AlreadyExistsException;
import com.github.qqrayzqq.cargoflow.repository.UserRepository;
import com.github.qqrayzqq.cargoflow.security.JwtService;
import com.github.qqrayzqq.cargoflow.security.UserDetailsPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private final AuthenticationManager authenticationManager;

    public String login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        UsernamePasswordAuthenticationToken data = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.password());
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(data);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Bad credentials");
        }
        UserDetailsPrincipal principal = (UserDetailsPrincipal) authenticate.getPrincipal();
        return jwtService.generateToken(principal);
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

        try {
            userRepository.save(newUser);
        } catch (IntegrityConstraintViolationException e) {
            throw new AlreadyExistsException("Username or email is already taken");
        }

        log.info("User {} registered", newUser.getUsername());
        return jwtService.generateToken(new UserDetailsPrincipal(newUser));
    }
}
