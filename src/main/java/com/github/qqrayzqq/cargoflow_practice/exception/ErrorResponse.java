package com.github.qqrayzqq.cargoflow_practice.exception;

import java.time.OffsetDateTime;

// Стандартное тело ошибки которое получит клиент в JSON.
// record — краткая запись неизменяемого класса с полями, геттерами и конструктором.
// Пример ответа:
// {
//   "status": 404,
//   "message": "User not found",
//   "timestamp": "2026-04-05T12:00:00Z"
// }
public record ErrorResponse(
        int status,           // HTTP статус код: 400, 401, 404, 409, 500...
        String message,       // человекочитаемое описание ошибки
        OffsetDateTime timestamp // когда произошла ошибка
) {
    // Фабричный метод — удобный способ создать ErrorResponse без повторения new ErrorResponse(...)
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, OffsetDateTime.now());
    }
}
