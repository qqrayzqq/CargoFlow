package com.github.qqrayzqq.cargoflow.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @GraphQlExceptionHandler(NotFoundException.class)
    public GraphQLError handleNotFound(NotFoundException ex, DataFetchingEnvironment env) {
        log.warn("Not found: {}", ex.getMessage());
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.NOT_FOUND)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

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
    public GraphQLError handleInvalidTransitionException(InvalidTransitionException ex, DataFetchingEnvironment env) {
        log.warn("Invalid status transition: {}", ex.getMessage());
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    @GraphQlExceptionHandler(BadRequestException.class)
    public GraphQLError handleBadRequestException(BadRequestException ex, DataFetchingEnvironment env) {
        log.warn("Bad request: {}", ex.getMessage());
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    // Reads jwt_error request attribute set by JwtFilter to return specific error codes
    @GraphQlExceptionHandler(AuthorizationDeniedException.class)
    public GraphQLError handleAccessDenied(AuthorizationDeniedException ex, DataFetchingEnvironment env) {
        String jwtError = getJwtError();

        if ("TOKEN_EXPIRED".equals(jwtError)) {
            log.warn("Expired token at {}", env.getExecutionStepInfo().getPath());
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message("Token expired")
                    .extensions(Map.of("code", "TOKEN_EXPIRED"))
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        }
        if ("TOKEN_INVALID".equals(jwtError)) {
            log.warn("Invalid token at {}", env.getExecutionStepInfo().getPath());
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message("Invalid token")
                    .extensions(Map.of("code", "TOKEN_INVALID"))
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        }

        log.warn("Access denied at {}", env.getExecutionStepInfo().getPath());
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.UNAUTHORIZED)
                .message("Access Denied")
                .extensions(Map.of("code", "ACCESS_DENIED"))
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }

    private String getJwtError() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return (String) attrs.getRequest().getAttribute("jwt_error");
        } catch (IllegalStateException e) {
            return null;
        }
    }

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
