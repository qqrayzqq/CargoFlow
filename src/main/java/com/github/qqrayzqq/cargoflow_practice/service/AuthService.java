package com.github.qqrayzqq.cargoflow_practice.service;

import com.github.qqrayzqq.cargoflow_practice.domain.User;
import com.github.qqrayzqq.cargoflow_practice.dto.user.LoginDto;
import com.github.qqrayzqq.cargoflow_practice.dto.user.RegisterDto;
import com.github.qqrayzqq.cargoflow_practice.exception.AlreadyExistsException;
import com.github.qqrayzqq.cargoflow_practice.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow_practice.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow_practice.repository.UserRepository;
import com.github.qqrayzqq.cargoflow_practice.security.JwtService;
import com.github.qqrayzqq.cargoflow_practice.security.UserDetailsPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginDto dto) {
        // Ищем пользователя по email. Если не найден — 404.
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Сравниваем введённый пароль с хэшем в БД через BCrypt.
        // Намеренно бросаем одно исключение для обоих случаев — не раскрываем что именно неверно.
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // Оборачиваем User в UserDetailsPrincipal и генерируем токен
        return jwtService.generateToken(new UserDetailsPrincipal(user));
    }

    public String register(RegisterDto dto) {
        // Проверяем что username и email ещё не заняты — если заняты, 409 Conflict
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new AlreadyExistsException("Username is already taken");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AlreadyExistsException("Email is already taken");
        }

        // Создаём нового пользователя — пароль хэшируем через BCrypt перед сохранением
        User newUser = new User(dto.getEmail(), dto.getUsername(), dto.getFullName(),
                passwordEncoder.encode(dto.getPassword()));

        userRepository.save(newUser); // сохраняем в БД

        // Сразу генерируем токен — пользователь залогинен после регистрации
        return jwtService.generateToken(new UserDetailsPrincipal(newUser));
    }
}
