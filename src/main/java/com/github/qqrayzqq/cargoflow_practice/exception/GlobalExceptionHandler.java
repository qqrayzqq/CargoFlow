package com.github.qqrayzqq.cargoflow_practice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice — говорит Spring: этот класс перехватывает исключения из всех контроллеров.
// Без него каждое непойманное исключение вернёт клиенту 500 Internal Server Error с HTML страницей.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler(X.class) — этот метод вызывается когда где-то в коде бросается исключение типа X.
    // Spring сам его поймает, вызовет нужный метод и вернёт клиенту правильный HTTP ответ.

    // 404 — ресурс не найден
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)                        // HTTP 404
                .body(ErrorResponse.of(404, ex.getMessage())); // тело ответа с сообщением из исключения
    }

    // 409 — конфликт, ресурс уже существует
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(AlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)                         // HTTP 409
                .body(ErrorResponse.of(409, ex.getMessage()));
    }

    // 401 — неверные учётные данные (логин/пароль)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)                     // HTTP 401
                .body(ErrorResponse.of(401, ex.getMessage()));
    }

    // 500 — всё остальное что мы не предусмотрели.
    // Ловим Exception — родитель всех исключений, срабатывает последним.
    // Клиенту не показываем детали — только общее сообщение, детали логируем на сервере.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)            // HTTP 500
                .body(ErrorResponse.of(500, "Internal server error"));
    }
}
