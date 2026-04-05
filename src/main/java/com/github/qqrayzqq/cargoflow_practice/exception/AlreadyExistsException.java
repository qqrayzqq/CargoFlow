package com.github.qqrayzqq.cargoflow_practice.exception;

// Бросается когда пытаются создать ресурс который уже существует.
// Например: регистрация с уже занятым email или username.
// Хендлер поймает это и вернёт клиенту 409 Conflict.
public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
