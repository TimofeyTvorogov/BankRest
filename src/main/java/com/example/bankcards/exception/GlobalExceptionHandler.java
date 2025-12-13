package com.example.bankcards.exception;


import com.example.bankcards.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации или некорректное состояние",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            CardHasExpiredException.class,
            CardNotActiveException.class,
            InsufficientFundsException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception e) {
        ErrorResponse response;
        if (e instanceof MethodArgumentNotValidException) {
            List<String> errors = ((MethodArgumentNotValidException) e)
                    .getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
            response = new ErrorResponse("Ошибка валидации", errors);
        }
        else {
            response = new ErrorResponse(e.getMessage());
        }

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(response);
    }

    @ApiResponse(
            responseCode = "404",
            description = "Запрашиваемый ресурс не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ExceptionHandler({
            UserNotFoundException.class,
            NoSuchElementException.class,
            CardNotFoundExcepion.class
    })
    public ResponseEntity<ErrorResponse> handleUserNotFound(Exception e) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ApiResponse(
            responseCode = "409",
            description = "Конфликт данных (ресурс уже существует)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ExceptionHandler({
            UserAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponse> handleUserAlreadyFound(UserAlreadyExistsException e) {
        return ResponseEntity
                .status(CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ApiResponse(
            responseCode = "403",
            description = "Доступ к ресурсу запрещен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ApiResponse(
            responseCode = "401",
            description = "Ошибка аутентификации или неверные учетные данные",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ExceptionHandler({
            BadCredentialsException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthException(Exception ex) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ApiResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleDefault(Exception ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR).build();
    }
}
