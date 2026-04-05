package com.github.qqrayzqq.cargoflow_practice.exception;

// Бросается когда ресурс не найден в БД — пользователь, посылка, перевозчик и т.д.
// Хендлер поймает это и вернёт клиенту 404 Not Found.
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message); // передаём сообщение в родительский класс RuntimeException
    }
}
