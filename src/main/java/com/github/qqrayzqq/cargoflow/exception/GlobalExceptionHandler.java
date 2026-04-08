package com.github.qqrayzqq.cargoflow.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j          // генерирует поле: private static final Logger log = LoggerFactory.getLogger(...)
@ControllerAdvice   // говорит Spring: этот класс перехватывает исключения из всех контроллеров и резолверов
public class GlobalExceptionHandler {

    // Вызывается когда где-то в коде бросается NotFoundException.
    // Возвращает GraphQLError — Spring GraphQL кладёт его в поле errors[] ответа.
    @GraphQlExceptionHandler(NotFoundException.class)   // слушаем конкретный тип исключения
    public GraphQLError handleNotFound(NotFoundException ex, DataFetchingEnvironment env) {
        log.warn("Not found: {}", ex.getMessage());     // пишем в лог на уровне WARN — ожидаемая ситуация, не баг
        return GraphqlErrorBuilder.newError()           // начинаем строить объект ошибки
                .errorType(ErrorType.NOT_FOUND)         // тип ошибки — попадёт в extensions.classification в ответе
                .message(ex.getMessage())               // текст ошибки — попадёт в errors[0].message
                .path(env.getExecutionStepInfo().getPath())         // путь резолвера: ["getShipmentById"]
                .location(env.getField().getSourceLocation())       // строка в GraphQL запросе где произошла ошибка
                .build();                               // собираем объект GraphQLError
    }

    // Вызывается при попытке создать дубликат (пользователь уже существует и т.д.)
    @GraphQlExceptionHandler(AlreadyExistsException.class)
    public GraphQLError handleAlreadyExist(AlreadyExistsException ex, DataFetchingEnvironment env) {
        log.warn("Already exists: {}", ex.getMessage());
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    // Вызывается при неверном логине или пароле
    @GraphQlExceptionHandler(InvalidCredentialsException.class)
    public GraphQLError handleInvalidCredentials(InvalidCredentialsException ex, DataFetchingEnvironment env) {
        log.warn("Auth failed: {}", ex.getMessage());
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.UNAUTHORIZED)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler(InvalidTransitionException.class)
    public GraphQLError handleInvalidTransitionException(InvalidTransitionException ex, DataFetchingEnvironment env){
        log.warn("Change status failed: {}", ex.getMessage());
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    // Последний рубеж — ловит всё что не поймали выше.
    // Клиенту не показываем детали (могут содержать внутреннюю информацию).
    // log.error с третьим аргументом ex — печатает полный stacktrace в лог файл.
    @GraphQlExceptionHandler(Exception.class)
    public GraphQLError handleGeneral(Exception ex, DataFetchingEnvironment env) {
        log.error("Unexpected error at {}: {}", env.getExecutionStepInfo().getPath(), ex.getMessage(), ex);
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Internal server error")
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}