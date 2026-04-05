package com.github.qqrayzqq.cargoflow_practice.exception;

// Бросается когда пользователь ввёл неверный логин или пароль.
// Хендлер поймает это и вернёт клиенту 401 Unauthorized.
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password"); // сообщение фиксированное — не раскрываем что именно неверно
    }
}
